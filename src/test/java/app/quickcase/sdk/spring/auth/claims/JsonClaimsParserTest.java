package app.quickcase.sdk.spring.auth.claims;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@DisplayName("JsonClaimsParser")
class JsonClaimsParserTest {

    @Nested
    @DisplayName("getString")
    class GetString {
        @Test
        @DisplayName("should return empty optional when claim missing")
        void claimMissing() {
            final ClaimsParser parser = new JsonClaimsParser(new HashMap<>());

            final Optional<String> missingClaim = parser.getString("any");

            assertThat(missingClaim.isEmpty(), is(true));
        }

        @Test
        @DisplayName("should return claim as String when present and textual")
        void claimPresentAsText() {
            final ClaimsParser parser = new JsonClaimsParser(mapWith("claim1", new TextNode("value1")));

            final Optional<String> claim1 = parser.getString("claim1");

            assertThat(claim1.orElseThrow(), equalTo("value1"));
        }

        @Test
        @DisplayName("should return empty optional when present but not textual")
        void claimPresentAsObject() {
            final ClaimsParser parser = new JsonClaimsParser(mapWith("claim1", new ObjectNode(new JsonNodeFactory(true))));

            final Optional<String> claim1 = parser.getString("claim1");

            assertThat(claim1.isEmpty(), is(true));
        }
    }

    @Nested
    @DisplayName("getNode")
    class GetNode {
        @Test
        @DisplayName("should return empty optional when claim missing")
        void claimMissing() {
            final JsonClaimsParser parser = new JsonClaimsParser(new HashMap<>());

            final Optional<JsonNode> missingClaim = parser.getNode("any");

            assertThat(missingClaim.isEmpty(), is(true));
        }

        @Test
        @DisplayName("should return claim as JsonNode when present")
        void claimPresentAsNode() {
            final ObjectNode node = new ObjectNode(new JsonNodeFactory(true));

            final JsonClaimsParser parser = new JsonClaimsParser(mapWith("claim1", node));

            final Optional<JsonNode> claim1 = parser.getNode("claim1");

            assertThat(claim1.orElseThrow(), equalTo(node));
        }

    }

    @Nested
    @DisplayName("getObject")
    class GetObject {
        @Test
        @DisplayName("should return empty optional when claim missing")
        void claimMissing() {
            final ClaimsParser parser = new JsonClaimsParser(new HashMap<>());

            final Optional<ObjectNode> missingClaim = parser.getObject("any");

            assertThat(missingClaim.isEmpty(), is(true));
        }

        @Test
        @DisplayName("should return ObjectNode claim as ObjectNode when present")
        void claimPresentAsObject() {
            final ObjectNode node = new ObjectNode(new JsonNodeFactory(true));
            final ClaimsParser parser = new JsonClaimsParser(mapWith("claim1", node));

            final Optional<ObjectNode> claim1 = parser.getObject("claim1");

            assertThat(claim1.orElseThrow(), equalTo(node));
        }

        @Test
        @DisplayName("should return empty optional when claim is non-object")
        void claimMissingAsNonObject() {
            final ArrayNode node = new ArrayNode(new JsonNodeFactory(true));
            final ClaimsParser parser = new JsonClaimsParser(mapWith("claim1", node));

            final Optional<ObjectNode> claim1 = parser.getObject("claim1");

            assertThat(claim1.isEmpty(), is(true));
        }

        @Test
        @DisplayName("should parse valid TextNode to return as ObjectNode")
        void claimPresentAsObjectString() {
            final TextNode node = new TextNode("{\"key\": \"value\"}");
            final ClaimsParser parser = new JsonClaimsParser(mapWith("claim1", node));

            final Optional<ObjectNode> claim1 = parser.getObject("claim1");

            final ObjectNode expected = new ObjectNode(new JsonNodeFactory(true)).put("key", "value");

            assertThat(claim1.orElseThrow(), equalTo(expected));
        }

        @Test
        @DisplayName("should ignore TextNode representing non-objects")
        void claimMissingAsArrayString() {
            final TextNode node = new TextNode("[]");
            final ClaimsParser parser = new JsonClaimsParser(mapWith("claim1", node));

            final Optional<ObjectNode> claim1 = parser.getObject("claim1");

            assertThat(claim1.isEmpty(), is(true));
        }


        @Test
        @DisplayName("should ignore non-parseable TextNode")
        void claimMissingAsNonParseableString() {
            final TextNode node = new TextNode("{\"key\"");
            final ClaimsParser parser = new JsonClaimsParser(mapWith("claim1", node));

            final Optional<ObjectNode> claim1 = parser.getObject("claim1");

            assertThat(claim1.isEmpty(), is(true));
        }
    }

    private Map<String, JsonNode> mapWith(String key, JsonNode value) {
        final HashMap<String, JsonNode> map = new HashMap<>();
        map.put(key, value);
        return map;
    }
}