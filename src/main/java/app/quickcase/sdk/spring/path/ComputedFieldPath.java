package app.quickcase.sdk.spring.path;

import java.util.regex.Pattern;

import lombok.NonNull;

public class ComputedFieldPath extends FieldPath {
    public static final Pattern PATTERN = Pattern.compile("^:[a-zA-Z0-9_]+$");

    protected ComputedFieldPath(@NonNull String path) {
        super(path);

        if (!accepts(path)) {
            throw new IllegalArgumentException("Invalid computed field path: " + path);
        }
    }

    public static boolean accepts(String path) {
        return PATTERN.matcher(path).matches();
    }

    public String getIdentifier() {
        return path.substring(1);
    }
}
