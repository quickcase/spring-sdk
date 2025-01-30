package app.quickcase.sdk.spring.path;

import java.util.regex.Pattern;

import lombok.NonNull;

public final class DataFieldPath extends FieldPath {
    public static final Pattern PATTERN = Pattern.compile("^[a-zA-Z0-9_]+(?:\\[(?:id:[a-zA-Z0-9_]+)|(?:[0-9]+)])?(?:\\.[a-zA-Z0-9_]+(?:\\[(?:id:[a-zA-Z0-9_]+)|(?:[0-9]+)])?)*$");

    DataFieldPath(@NonNull String path) {
        super(path);
    }
}
