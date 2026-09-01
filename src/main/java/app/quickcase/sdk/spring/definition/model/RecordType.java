package app.quickcase.sdk.spring.definition.model;

import lombok.Builder;
import lombok.NonNull;

@Builder
public record RecordType(
        @NonNull Schema schema,
        @NonNull Workflow workflow
) {
}
