package app.quickcase.sdk.spring.data;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

import app.quickcase.sdk.spring.definition.model.DataField;
import app.quickcase.sdk.spring.path.DataFieldPath;
import app.quickcase.sdk.spring.path.FieldPath;
import tools.jackson.databind.JsonNode;
import lombok.NonNull;

public interface DataIterator extends Iterator<DataIterator.Entry> {
    /**
     * Expose the iterator as a {@link Stream} for convenience.
     *
     * @return A stream of the current iterator.
     */
    Stream<Entry> stream();

    record Entry(
            @NonNull DataFieldPath path,
            @NonNull Optional<DataField> field,
            @NonNull JsonNode value
    ) {
        public Entry(@NonNull String path, DataField field, @NonNull JsonNode value) {
            this(FieldPath.ofData(path), Optional.ofNullable(field), value);
        }

        public Entry(@NonNull String path, @NonNull JsonNode value) {
            this(FieldPath.ofData(path), Optional.empty(), value);
        }
    }
}
