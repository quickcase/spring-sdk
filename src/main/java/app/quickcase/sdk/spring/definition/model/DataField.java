package app.quickcase.sdk.spring.definition.model;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

@Builder
public record DataField(
    @NonNull String id,
    @NonNull String name,
    String label,
    String hint,
    @NonNull String type,
    @Singular List<Option> options,
    Content content,
    @Singular Map<String, DataField> members,
    Validation validation,
    Field.Display display,
    @NonNull @Singular("acl") Map<String, Integer> acl,
    @NonNull String classification
) implements Field {
    @Builder
    public record Validation(
            String min,
            String max,
            String pattern
    ) {}

    @Builder
    public record Option(
            @NonNull String code,
            @NonNull String label
    ) {}

    @Builder
    public record Content(
            @NonNull String type,
            @Singular List<Option> options,
            @Singular Map<String, DataField> members
    ) {}
}
