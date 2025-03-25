package app.quickcase.sdk.spring.auth.converters;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class JwtScopesConverterTest {

    @Test
    @DisplayName("should return empty set when no jwt scope claim")
    void shouldReturnEmptySetWhenNoJwtScopesClaim() {
        var jwt = jwt().build();

        var converter = new JwtScopesConverter();

        assertThat(converter.convert(jwt), is(empty()));
    }

    @Test
    @DisplayName("should return set of parsed scopes from `scope` claim")
    void shouldReturnSetOfParsedScopesFromScopeClaim() {
        var jwt = jwt().claim("scope", "scope1 scope2 scope3").build();

        var converter = new JwtScopesConverter();

        assertThat(converter.convert(jwt), containsInAnyOrder("scope1", "scope2", "scope3"));
    }

    @Test
    @DisplayName("should return set of parsed scopes from `scp` claim")
    void shouldReturnSetOfParsedScopesFromScpClaim() {
        var jwt = jwt().claim("scope", "scope1 scope2 scope3").build();

        var converter = new JwtScopesConverter();

        assertThat(converter.convert(jwt), containsInAnyOrder("scope1", "scope2", "scope3"));
    }

    private Jwt.Builder jwt() {
        return Jwt.withTokenValue("token")
                  .header("alg", "none")
                  .claim("claim1", "value1");
    }
}