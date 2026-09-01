package app.quickcase.sdk.spring.definition.model;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

@Builder
public record CreateAction(
    @NonNull String id,
    @NonNull String name,
    String label,
    String description,
    Integer order,
    @NonNull String toState,
    @Singular List<Action.Postcondition> postconditions,
    @NonNull @Singular("acl") Map<String, Integer> acl,
    @NonNull String classification,
    Action.Webhooks webhooks
) {
}
