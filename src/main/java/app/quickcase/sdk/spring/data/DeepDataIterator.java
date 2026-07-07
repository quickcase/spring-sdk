package app.quickcase.sdk.spring.data;

import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import app.quickcase.sdk.spring.definition.DefinitionExtractor;
import app.quickcase.sdk.spring.definition.model.DataField;
import app.quickcase.sdk.spring.definition.model.RecordType;
import app.quickcase.sdk.spring.path.DataFieldPath;
import app.quickcase.sdk.spring.path.FieldPath;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

/**
 * <p>
 *     A depth-first iterator for recursive traversal of record data.
 * </p>
 * <p>
 *     This iterator is driven by data properties and accepts that a corresponding data field may not be found in schema
 *     definition. This scenario is materialised by the iterated entry's field property being optional. It is the
 *     responsibility of the iterator' consumers to decide how to react to entries missing definitions.
 * </p>
 * <p>
 *     For composite types (Complex, Collection), the composite field itself will be yielded before its components are
 *     themselves iterated. For example, given a Collection of Complex `collection1`, the following nodes will be iterated:
 * </p>
 * <ol>
 *     <li><code>collection1</code>: Top-level collection field</li>
 *     <li><code>collection1[id:item1].value</code>: The complex field for the first item</li>
 *     <li><code>collection1[id:item1].value.member1</code></li>
 *     <li><code>collection1[id:item1].value.member2</code></li>
 *     <li><code>collection1[id:item2].value</code>: The complex field for the second item</li>
 *     <li><code>collection1[id:item2].value.member1</code></li>
 *     <li><code>collection1[id:item2].value.member2</code></li>
 * </ol>
 * <p>
 *     For collection items, priority is always given to using items' ID for the path whenever defined and non-blank.
 *     When missing, the item positional index is used as a fallback.
 * </p>
 */
public class DeepDataIterator implements DataIterator {

    private final DefinitionExtractor definition;
    private final Stack<DataFieldIterator> iterators = new Stack<>();

    public DeepDataIterator(RecordType type, ObjectNode data) {
        definition = new DefinitionExtractor(type);
        iterators.push(new RootFieldIterator(data.propertyStream().iterator()));
    }

    @Override
    public boolean hasNext() {
        while (!iterators.isEmpty() && !iterators.peek().hasNext()) {
            // De-stack completed iterators until all iterators are completed
            iterators.pop();
        }
        return !iterators.isEmpty() && iterators.peek().hasNext();
    }

    @Override
    public Entry next() {
        var context = iterators.peek();
        var entry = context.next();
        var path = entry.getKey();
        var field = definition.extractField(path);
        var value = entry.getValue();

        field.ifPresent(dataField -> {
            if (value.isObject() && DataField.TYPE_COMPLEX.equals(dataField.type())) {
                // Next we iterate all complex members
                iterators.push(new ComplexMembersIterator(path, value.propertyStream().iterator()));
            }

            if (value.isArray() && DataField.TYPE_COLLECTION.equals(dataField.type())) {
                // Next we iterate all collection items
                iterators.push(new CollectionItemsIterator(path, value.iterator()));
            }
        });

        return new Entry(path, field, value);
    }

    @Override
    public Stream<Entry> stream() {
        Iterable<Entry> iterable = () -> this;
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    private interface DataFieldIterator extends Iterator<Map.Entry<DataFieldPath, JsonNode>> {
    }

    private record RootFieldIterator(
            @NonNull Iterator<Map.Entry<String, JsonNode>> iterator
    ) implements DataFieldIterator {
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Map.Entry<DataFieldPath, JsonNode> next() {
            var next = iterator.next();
            return Map.entry(
                    FieldPath.ofData(next.getKey()),
                    next.getValue()
            );
        }
    }

    private record ComplexMembersIterator(
            @NonNull DataFieldPath complexPath,
            @NonNull Iterator<Map.Entry<String, JsonNode>> iterator
    ) implements DataFieldIterator {
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Map.Entry<DataFieldPath, JsonNode> next() {
            var next = iterator.next();
            return Map.entry(
                    complexPath.appendMember(next.getKey()),
                    next.getValue()
            );
        }
    }

    private static class CollectionItemsIterator implements DataFieldIterator {

        private final DataFieldPath collectionPath;
        private final Iterator<JsonNode> iterator;
        private int index = 0;

        public CollectionItemsIterator(@NonNull DataFieldPath collectionPath, @NonNull Iterator<JsonNode> iterator) {
            this.collectionPath = collectionPath;
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Map.Entry<DataFieldPath, JsonNode> next() {
            var itemIndex = index++;
            var item = (ObjectNode) iterator.next();
            var id = item.get("id");
            var itemPath = (id != null && StringUtils.hasText(id.stringValue())) ?
                    collectionPath.appendItemSelector(id.stringValue(), itemIndex) : collectionPath.appendItemSelector(itemIndex);

            return Map.entry(
                    itemPath.appendMember("value"),
                    item.get("value")
            );
        }
    }
}
