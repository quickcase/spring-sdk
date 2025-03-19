package app.quickcase.sdk.spring.auth.claims;

import java.util.Optional;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface ClaimsParser {
    Optional<String> getString(String claim);

    Optional<ObjectNode> getObject(String claim);
}
