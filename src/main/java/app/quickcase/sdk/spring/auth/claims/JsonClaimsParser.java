package app.quickcase.sdk.spring.auth.claims;

import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonClaimsParser implements ClaimsParser {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Map<String, JsonNode> claims;

    public JsonClaimsParser(Map<String, JsonNode> claims) {
        this.claims = claims;
    }

    @Override
    public Optional<String> getString(String claim) {
        log.debug("Extracting claim `{}` as String", claim);
        return getNode(claim).map(JsonNode::textValue);
    }

    public Optional<JsonNode> getNode(String claim) {
        return Optional.ofNullable(claims.get(claim));
    }

    @Override
    public Optional<ObjectNode> getObject(String claim) {
        log.debug("Extracting claim `{}` as JSON object", claim);
        return getNode(claim).flatMap(jsonNode -> parseTextNode(claim, jsonNode))
                             .filter(JsonNode::isObject)
                             .map(JsonNode::deepCopy);
    }

    private Optional<JsonNode> parseTextNode(String claim, JsonNode node) {
        if (node.isTextual()) {
            try {
                return Optional.of(MAPPER.readTree(node.textValue()));
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse JSON object for claim `{}`, got: `{}`", claim, node.textValue());
                return Optional.empty();
            }
        }

        return Optional.of(node);
    }
}
