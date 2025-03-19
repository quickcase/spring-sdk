package app.quickcase.sdk.spring.auth.claims;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@DisplayName("JwtClaimsParser")
class JwtClaimsParserTest {

    @Nested
    @DisplayName("getString")
    class GetString {
        @Test
        @DisplayName("should return empty optional when claim missing")
        void claimMissing() {
            final ClaimsParser parser = new JwtClaimsParser(new HashMap<>());

            final Optional<String> missingClaim = parser.getString("any");

            assertThat(missingClaim.isEmpty(), is(true));
        }

        @Test
        @DisplayName("should return claim as String when present as String")
        void claimPresentAsString() {
            final ClaimsParser parser = new JwtClaimsParser(mapWith("claim1", "value1"));

            final Optional<String> claim1 = parser.getString("claim1");

            assertThat(claim1.orElseThrow(), equalTo("value1"));
        }

        @Test
        @DisplayName("should cast value to String when present but not String")
        void claimPresentAsOther() {
            final ClaimsParser parser = new JwtClaimsParser(mapWith("claim1", 1));

            final Optional<String> claim1 = parser.getString("claim1");

            assertThat(claim1.orElseThrow(), equalTo("1"));
        }
    }

    @Nested
    @DisplayName("getObject")
    class GetObject {
        @Test
        @DisplayName("should return empty optional when claim missing")
        void claimMissing() {
            final ClaimsParser parser = new JwtClaimsParser(new HashMap<>());

            final Optional<ObjectNode> missingClaim = parser.getObject("any");

            assertThat(missingClaim.isEmpty(), is(true));
        }

        @Test
        @DisplayName("should parse JSON string claim into ObjectNode when present")
        void claimPresentAsJsonString() {
            final ClaimsParser parser = new JwtClaimsParser(mapWith("claim1", "{\"key\": \"value\"}"));

            final Optional<ObjectNode> claim1 = parser.getObject("claim1");

            assertThat(claim1.orElseThrow().get("key").asText(), equalTo("value"));
        }

        @Test
        @DisplayName("should return empty optional when claim is non-object")
        void claimMissingAsNonObject() {
            final ClaimsParser parser = new JwtClaimsParser(mapWith("claim1", "[]"));

            final Optional<ObjectNode> claim1 = parser.getObject("claim1");

            assertThat(claim1.isEmpty(), is(true));
        }


        @Test
        @DisplayName("should ignore non-parseable claims (JSON not valid)")
        void claimMissingAsNonParseableString() {
            final ClaimsParser parser = new JwtClaimsParser(mapWith("claim1", "{\"key\""));

            final Optional<ObjectNode> claim1 = parser.getObject("claim1");

            assertThat(claim1.isEmpty(), is(true));
        }
    }

    private Map<String, Object> mapWith(String key, Object value) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }
}