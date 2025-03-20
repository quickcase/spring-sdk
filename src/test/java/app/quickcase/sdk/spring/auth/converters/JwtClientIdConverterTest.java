package app.quickcase.sdk.spring.auth.converters;

import java.util.Optional;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtClientIdConverter")
class JwtClientIdConverterTest {

    final JwtClientIdConverter converter = new JwtClientIdConverter();

    @Test
    @DisplayName("should return client ID from claim `client_id`")
    void shouldReturnClientIdFromClaimClientId() {
        var jwt = jwtBuilder().claim("client_id", "aClientId").build();

        assertThat(converter.convert(jwt), is("aClientId"));
    }

    @Test
    @DisplayName("should return client ID from claim `azp`")
    void shouldReturnClientIdFromClaimAzp() {
        var jwt = jwtBuilder().claim("azp", "aClientId").build();

        assertThat(converter.convert(jwt), is("aClientId"));
    }

    @Test
    @DisplayName("should give precedence to claim `client_id` vs `azp`")
    void shouldGivePrecedenceToClaimClientId() {
        var jwt = jwtBuilder().claim("azp", "incorrect")
                              .claim("client_id", "aClientId")
                              .build();

        assertThat(converter.convert(jwt), is("aClientId"));
    }

    @Test
    @DisplayName("should return empty when no relevant claim found")
    void shouldReturnEmptyWhenNoRelevantClaimFound() {
        var jwt = jwtBuilder().build();

        assertThat(converter.convert(jwt), is(nullValue()));
    }

    private Jwt.Builder jwtBuilder() {
        return Jwt.withTokenValue("token-value")
                   .header("alg", "none")
                   .claim("claim1", "value1");
    }

}