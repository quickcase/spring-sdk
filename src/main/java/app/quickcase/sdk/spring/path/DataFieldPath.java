package app.quickcase.sdk.spring.path;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.NonNull;
import org.springframework.lang.Nullable;

public final class DataFieldPath extends FieldPath {
    private static final String MEMBER_SEPARATOR = ".";
    private static final Pattern PATTERN = Pattern.compile("^[a-zA-Z0-9_]+(?:\\[(?:(?:id:[a-zA-Z0-9_-]+)|(?:[0-9]+))?])?(?:\\.[a-zA-Z0-9_]+(?:\\[(?:(?:id:[a-zA-Z0-9_-]+)|(?:[0-9]+))?])?)*$");
    private static final Pattern COLLECTION_ITEM_PATTERN = Pattern.compile("^(?<collectionId>[a-zA-Z0-9_]+)\\[(?:(?:id:(?<itemId>[a-zA-Z0-9_-]+))|(?<itemIndex>[0-9]+))?]$");

    @Nullable
    private String alternativePath;

    DataFieldPath(@NonNull String path) {
        super(path);

        if (!accepts(path)) {
            throw new IllegalArgumentException("Invalid data field path: " + path);
        }
    }

    DataFieldPath(@NonNull String path, String alternativePath) {
        this(path);
        this.alternativePath = alternativePath;
    }

    public List<Element> elements() {
        return Arrays.stream(path.split("\\."))
                     .map(Element::new)
                     .toList();
    }

    public DataFieldPath appendMember(@NonNull String memberId) {
        final var memberPath = this.path + MEMBER_SEPARATOR + memberId;

        if (alternativePath != null) {
            return new DataFieldPath(memberPath, this.alternativePath + MEMBER_SEPARATOR + memberId);
        }

        return new DataFieldPath(memberPath);
    }

    public DataFieldPath appendItemSelector(@NonNull String itemId) {
        final var itemPath = this.path + "[id:" + itemId + "]";

        if (alternativePath != null) {
            return new DataFieldPath(itemPath, this.alternativePath + "[id:" + itemId + "]");
        }

        return new DataFieldPath(itemPath);
    }

    public DataFieldPath appendItemSelector(int index) {
        final var itemPath = this.path + "[" + index + "]";

        if (alternativePath != null) {
            return new DataFieldPath(itemPath, this.alternativePath + "[" + index + "]");
        }

        return new DataFieldPath(itemPath);
    }

    public DataFieldPath appendItemSelector(@NonNull String itemId, int index) {
        final var itemPath = this.path + "[id:" + itemId + "]";

        if (alternativePath != null) {
            return new DataFieldPath(itemPath, this.alternativePath + "[" + index + "]");
        }

        return new DataFieldPath(itemPath, this.path + "[" + index + "]");
    }

    /**
     * Paths to collection items can be expressed in 2 ways: either by item ID or by item index.
     * While item ID should always be favoured, for backward compatibility it is sometimes required to use the index.
     * When both paths are known, the index-based path is exposed via this getter.
     * <p>
     *     <strong>Attention: </strong> Recording of index-based path as alternative is by convention only, it is the
     *     responsibility of the {@link DataFieldPath} creator to favour item IDs as the primary path.
     * </p>
     * @return The index-based path when both ID-based and index-based path are known, null otherwise.
     */
    @Nullable
    public String getAlternativePath() {
        return alternativePath;
    }

    public static boolean accepts(String path) {
        return PATTERN.matcher(path).matches();
    }

    @Getter
    public static class Element {
        private final String identifier;
        private final boolean collectionItem;
        private final String collectionIdentifier;
        private final String itemId;
        private final Integer itemIndex;

        public Element(@NonNull String identifier) {
            this.identifier = identifier;

            var collectionItemMatcher = DataFieldPath.COLLECTION_ITEM_PATTERN.matcher(identifier);
            collectionItem = collectionItemMatcher.find();

            collectionIdentifier = collectionItem ? collectionItemMatcher.group("collectionId") : null;
            itemId = collectionItem ? collectionItemMatcher.group("itemId") : null;
            var index = collectionItem ? collectionItemMatcher.group("itemIndex") : null;
            itemIndex = (index != null) ? Integer.parseInt(index) : null;
        }
    }
}
