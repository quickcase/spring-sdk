package app.quickcase.sdk.spring.definition.model;

import java.util.Map;
import java.util.Optional;

import lombok.NonNull;

public interface Field {
    @NonNull String id();
    @NonNull String name();
    String label();
    Field.Display display();

    interface Display {
        String mode();
        Map<String, String> parameters();
    }
}
