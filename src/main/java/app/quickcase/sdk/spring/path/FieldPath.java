package app.quickcase.sdk.spring.path;

import lombok.EqualsAndHashCode;
import lombok.NonNull;

@EqualsAndHashCode
abstract public class FieldPath {
    protected final String path;

    protected FieldPath(@NonNull String path) {
        this.path = path;
    }

    public static FieldPath of(@NonNull String path) {
        if (MetadataFieldPath.PATTERN.matcher(path).matches()) {
            return ofMetadata(path);
        }

        return ofData(path);
    }

    public static MetadataFieldPath ofMetadata(@NonNull String path) {
        return new MetadataFieldPath(path);
    }

    public static DataFieldPath ofData(@NonNull String path) {
        if (!DataFieldPath.PATTERN.matcher(path).matches()) {
            throw new IllegalArgumentException("Invalid data field path: " + path);
        }

        return new DataFieldPath(path);
    }

    @Override
    public String toString() {
        return path;
    }
}
