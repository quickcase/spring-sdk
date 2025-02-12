package app.quickcase.sdk.spring.definition.model;

import java.util.Map;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

public interface Field {
    @NonNull String id();
    @NonNull String name();
    String label();
    Field.Display display();

    @Builder
    record Display(
            String mode,
            @Singular(ignoreNullCollections = true) Map<String, String> parameters
    ) {
    }
}
