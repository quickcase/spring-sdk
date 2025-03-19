package app.quickcase.sdk.spring.auth.claims;

import java.util.Optional;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface ClaimsParser {
    Optional<String> getString(String claim);

    /**
     * @deprecated JSON claims should not be used, simple textual key value pairs should be preferred; scheduled for removal in v2.0.0.
     */
    @Deprecated(forRemoval = true)
    Optional<ObjectNode> getObject(String claim);
}
