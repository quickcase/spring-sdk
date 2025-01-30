package app.quickcase.sdk.spring.definition.model;

import java.util.List;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

@Builder
public record MetadataField(
        @NonNull String id,
        @NonNull String name,
        String label,
        @Singular List<MetadataField.Option> options,
        Field.Display display
) implements Field{
    @Builder
    public record Option(
            @NonNull String code,
            @NonNull String label
    ) {}
}
