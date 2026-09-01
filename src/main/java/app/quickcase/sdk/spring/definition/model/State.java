package app.quickcase.sdk.spring.definition.model;

import java.util.Map;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

@Builder
public record State(
    @NonNull String id,
    @NonNull String name,
    String label,
    String description,
    String titleTemplate,
    Integer order,
    @NonNull @Singular("acl") Map<String, Integer> acl
) {
}
