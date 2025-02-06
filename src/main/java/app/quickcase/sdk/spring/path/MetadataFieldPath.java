package app.quickcase.sdk.spring.path;

import java.util.Arrays;

import app.quickcase.sdk.spring.metadata.Metadata;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class MetadataFieldPath extends FieldPath {
    final private Metadata metadata;

    MetadataFieldPath(@NonNull String path) {
        super(path);
        metadata = Metadata.fromPath(path);
    }

    public static boolean accepts(@NonNull String path) {
        return Arrays.stream(Metadata.values()).anyMatch(meta -> meta.getPath().equalsIgnoreCase(path));
    }
}
