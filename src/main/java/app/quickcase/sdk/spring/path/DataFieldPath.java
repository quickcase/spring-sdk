package app.quickcase.sdk.spring.path;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.NonNull;

public final class DataFieldPath extends FieldPath {
    public static final Pattern PATTERN = Pattern.compile("^[a-zA-Z0-9_]+(?:\\[(?:(?:id:[a-zA-Z0-9_]+)|(?:[0-9]+))?])?(?:\\.[a-zA-Z0-9_]+(?:\\[(?:(?:id:[a-zA-Z0-9_]+)|(?:[0-9]+))?])?)*$");
    public static final Pattern COLLECTION_ITEM_PATTERN = Pattern.compile("^(?<collectionId>[a-zA-Z0-9_]+)\\[(?:(?:id:(?<itemId>[a-zA-Z0-9_]+))|(?<itemIndex>[0-9]+))?]$");

    DataFieldPath(@NonNull String path) {
        super(path);
    }

    public List<Element> elements() {
        return Arrays.stream(path.split("\\."))
                     .map(Element::new)
                     .toList();
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
