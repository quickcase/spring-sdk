package app.quickcase.sdk.spring.path;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.NonNull;

public final class DataFieldPath extends FieldPath {
    private static final String MEMBER_SEPARATOR = ".";
    private static final Pattern PATTERN = Pattern.compile("^[a-zA-Z0-9_]+(?:\\[(?:(?:id:[a-zA-Z0-9_]+)|(?:[0-9]+))?])?(?:\\.[a-zA-Z0-9_]+(?:\\[(?:(?:id:[a-zA-Z0-9_]+)|(?:[0-9]+))?])?)*$");
    private static final Pattern COLLECTION_ITEM_PATTERN = Pattern.compile("^(?<collectionId>[a-zA-Z0-9_]+)\\[(?:(?:id:(?<itemId>[a-zA-Z0-9_]+))|(?<itemIndex>[0-9]+))?]$");

    DataFieldPath(@NonNull String path) {
        super(path);

        if (!accepts(path)) {
            throw new IllegalArgumentException("Invalid data field path: " + path);
        }
    }

    public List<Element> elements() {
        return Arrays.stream(path.split("\\."))
                     .map(Element::new)
                     .toList();
    }

    public DataFieldPath appendMember(@NonNull String memberId) {
        return new DataFieldPath(this.path + MEMBER_SEPARATOR + memberId);
    }

    public DataFieldPath appendItemSelector(@NonNull String itemId) {
        return new DataFieldPath(this.path + "[id:" + itemId + "]");
    }

    public DataFieldPath appendItemSelector(int index) {
        return new DataFieldPath(this.path + "[" + index + "]");
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
