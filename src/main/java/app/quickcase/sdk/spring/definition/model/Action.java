package app.quickcase.sdk.spring.definition.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

@Builder
public record Action(
    @NonNull String id,
    @NonNull String name,
    String label,
    String description,
    Integer order,
    @Singular Set<String> fromStates,
    String precondition,
    String toState,
    @Singular List<Postcondition> postconditions,
    @NonNull @Singular("acl") Map<String, Integer> acl,
    @NonNull String classification,
    Webhooks webhooks
) {
    @Builder
    public record Postcondition(
            @NonNull String path,
            @NonNull String value
    ) {
    }

    @Builder
    public record Webhooks(
            @NonNull Webhook onStart,
            @NonNull Webhook onSubmit,
            @NonNull Webhook onSubmitted
    ) {
    }
}
