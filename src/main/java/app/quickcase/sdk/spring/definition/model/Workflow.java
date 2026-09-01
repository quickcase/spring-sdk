package app.quickcase.sdk.spring.definition.model;

import java.util.Map;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

@Builder
public record Workflow(
        @NonNull @Singular Map<String, State> states,
        @NonNull @Singular Map<String, CreateAction> createActions,
        @NonNull @Singular Map<String, Action> actions
) {
}
