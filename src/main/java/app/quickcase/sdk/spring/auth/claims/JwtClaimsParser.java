package app.quickcase.sdk.spring.auth.claims;

import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtClaimsParser implements ClaimsParser {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Map<String, Object> claims;

    public JwtClaimsParser(Map<String, Object> claims) {
        this.claims = claims;
    }

    @Override
    public Optional<String> getString(String claim) {
        return Optional.ofNullable(claims.get(claim))
                       .map(Object::toString);
    }

    @Override
    public Optional<ObjectNode> getObject(String claim) {
        log.debug("Extracting claim `{}` as JSON object", claim);
        return getString(claim).flatMap(jsonNode -> parseJsonClaim(claim, jsonNode))
                             .filter(JsonNode::isObject)
                             .map(JsonNode::deepCopy);
    }

    private Optional<JsonNode> parseJsonClaim(String claim, String value) {
        try {
            return Optional.of(MAPPER.readTree(value));
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse JSON object for claim `{}`, got: `{}`", claim, value);
            return Optional.empty();
        }
    }
}
