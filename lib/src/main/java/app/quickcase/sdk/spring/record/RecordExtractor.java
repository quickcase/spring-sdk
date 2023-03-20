package app.quickcase.sdk.spring.record;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class RecordExtractor {
    public static final String OBJECT_SEPARATOR = "\\.";
    private static final Pattern PATH_ELEMENT_PATTERN = Pattern.compile("^(?<name>[^\\[\\]]+)(?:\\[(?:(?<colIndex>\\d+)|id:(?<colId>[^\\[\\]]+))\\])?$");
    private static final char METADATA_START = '[';

    private final Record record;

    public RecordExtractor(Record record) {
        if (record == null) {
            throw new NullPointerException("A record must be provided");
        }
        this.record = record;
    }

    public Optional<JsonNode> extract(String path) {
        if (path == null) {
            throw new NullPointerException("A path must be provided");
        }

        if (path.charAt(0) == METADATA_START) {
            return extractFromMetadata(path);
        }

        final PathElement[] pathElements = Arrays.stream(path.split(OBJECT_SEPARATOR))
                                                 .map(this::parsePathElement)
                                                 .toArray(PathElement[]::new);

        return extract(record.getData(), pathElements);
    }

    private Optional<JsonNode> extractFromMetadata(String path) {
        final String metadata = path.substring(1, path.length() - 1).toLowerCase();
        return switch (metadata) {
            case "workspace", "organisation", "jurisdiction" -> optionalTextNode(record.getWorkspace());
            case "type", "case_type" -> optionalTextNode(record.getType());
            case "state" -> optionalTextNode(record.getState());
            case "id", "reference", "case_reference" -> optionalTextNode(record.getReference());
            case "classification", "security_classification" -> optionalTextNode(record.getClassification());
            case "created", "created_date" -> optionalTextNode(record.getCreated().toString());
            case "modified", "last_modified" -> optionalTextNode(record.getLastModified().toString());
            default -> Optional.empty();
        };
    }

    private Optional<JsonNode> optionalTextNode(String value) {
        return Optional.of(new TextNode(value));
    }

    private PathElement parsePathElement(String pathElement) {
        final Matcher matcher = PATH_ELEMENT_PATTERN.matcher(pathElement);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Empty or invalid path element: " + pathElement);
        }

        final String colIndex = matcher.group("colIndex");

        return new PathElement(matcher.group("name"),
                               matcher.group("colId"),
                               colIndex != null ? Integer.parseInt(colIndex) : null);
    }

    private Optional<JsonNode> extract(ObjectNode from, PathElement[] pathElements) {
        final PathElement head = pathElements[0];
        final PathElement[] tail = Arrays.copyOfRange(pathElements, 1, pathElements.length);

        final Optional<JsonNode> node = head.isCollectionItem() ?
                extractFromCollection(from, head) : extractFromObject(from, head);

        return node.flatMap((value) -> {
            if (tail.length == 0) {
                return Optional.of(value);
            }

            if (value.isObject()) {
                return extract((ObjectNode) value, tail);
            }

            return Optional.empty();
        });
    }

    private Optional<JsonNode> extractFromObject(ObjectNode from, PathElement pathElement) {
        final JsonNode value = from.get(pathElement.getName());

        if (value == null || value.isNull()) {
            return Optional.empty();
        }

        return Optional.of(value);
    }

    private Optional<JsonNode> extractFromCollection(ObjectNode from, PathElement pathElement) {
        return extractFromObject(from, pathElement).flatMap((value) -> {
            if (!value.isArray()) {
                return Optional.empty();
            }

            final ArrayNode collection = (ArrayNode) value;

            if (pathElement.getCollectionId().isPresent()) {
                final String itemId = pathElement.getCollectionId().get();
                return StreamSupport.stream(collection.spliterator(), false)
                                    .filter((item) -> {
                                        final JsonNode id = item.get("id");

                                        if (id == null) {
                                            return false;
                                        }

                                        return itemId.equals(id.textValue());
                                    }).findFirst();
            }

            return pathElement.getCollectionIndex().map(collection::get);
        });
    }

    private static class PathElement {
        private final String name;
        private final String collectionId;
        private final Integer collectionIndex;

        public PathElement(String name, String collectionId, Integer collectionIndex) {
            this.name = name;
            this.collectionId = collectionId;
            this.collectionIndex = collectionIndex;
        }

        public String getName() {
            return name;
        }

        public Optional<String> getCollectionId() {
            return Optional.ofNullable(collectionId);
        }

        public Optional<Integer> getCollectionIndex() {
            return Optional.ofNullable(collectionIndex);
        }

        public Boolean isCollectionItem() {
            return collectionId != null || collectionIndex != null;
        }
    }
}
