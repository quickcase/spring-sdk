package app.quickcase.sdk.spring.auth.claims;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

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
            var parser = new JwtClaimsParser(jwtBuilder().claim("claim1", "value1").build());

            var missingClaim = parser.getString("any");

            assertThat(missingClaim.isEmpty(), is(true));
        }

        @Test
        @DisplayName("should return claim as String when present as String")
        void claimPresentAsString() {
            var jwt = jwtBuilder().claim("claim1", "value1").build();
            var parser = new JwtClaimsParser(jwt);

            var claim1 = parser.getString("claim1");

            assertThat(claim1.orElseThrow(), equalTo("value1"));
        }

        @Test
        @DisplayName("should cast value to String when present but not String")
        void claimPresentAsOther() {
            var jwt = jwtBuilder().claim("claim1", 1).build();
            var parser = new JwtClaimsParser(jwt);

            var claim1 = parser.getString("claim1");

            assertThat(claim1.orElseThrow(), equalTo("1"));
        }
    }

    @Nested
    @DisplayName("getObject")
    class GetObject {
        @Test
        @DisplayName("should return empty optional when claim missing")
        void claimMissing() {
            var parser = new JwtClaimsParser(jwtBuilder().claim("claim1", "value1").build());

            var missingClaim = parser.getObject("any");

            assertThat(missingClaim.isEmpty(), is(true));
        }

        @Test
        @DisplayName("should parse JSON string claim into ObjectNode when present")
        void claimPresentAsJsonString() {
            var jwt = jwtBuilder().claim("claim1", "{\"key\": \"value\"}").build();
            var parser = new JwtClaimsParser(jwt);

            var claim1 = parser.getObject("claim1");

            assertThat(claim1.orElseThrow().get("key").asText(), equalTo("value"));
        }

        @Test
        @DisplayName("should return empty optional when claim is non-object")
        void claimMissingAsNonObject() {
            var jwt = jwtBuilder().claim("claim1", "[]").build();
            var parser = new JwtClaimsParser(jwt);

            var claim1 = parser.getObject("claim1");

            assertThat(claim1.isEmpty(), is(true));
        }


        @Test
        @DisplayName("should ignore non-parseable claims (JSON not valid)")
        void claimMissingAsNonParseableString() {
            var jwt = jwtBuilder().claim("claim1", "{\"key\"").build();
            var parser = new JwtClaimsParser(jwt);

            var claim1 = parser.getObject("claim1");

            assertThat(claim1.isEmpty(), is(true));
        }
    }

    private Jwt.Builder jwtBuilder() {
        return Jwt.withTokenValue("token")
                  .header("alg", "none");
    }
}