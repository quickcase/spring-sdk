package app.quickcase.sdk.spring.definition.model;

import java.util.Map;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

@Builder
public record Schema(
        @NonNull @Singular Map<String, DataField> fields
) {
}
