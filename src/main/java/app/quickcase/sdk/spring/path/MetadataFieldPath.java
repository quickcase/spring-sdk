package app.quickcase.sdk.spring.path;

import java.util.regex.Pattern;

import app.quickcase.sdk.spring.metadata.Metadata;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class MetadataFieldPath extends FieldPath {
    public static final Pattern PATTERN = Pattern.compile("^\\[[a-zA-Z_]+]$");

    final private Metadata metadata;

    MetadataFieldPath(@NonNull String path) {
        super(path);
        metadata = Metadata.fromPath(path);
    }

}
