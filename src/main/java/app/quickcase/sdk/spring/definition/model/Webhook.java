package app.quickcase.sdk.spring.definition.model;

import java.net.URL;
import java.util.List;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

@Builder
public record Webhook(
        @NonNull URL url,
        @Singular @NonNull List<Integer> retries
) {
}
