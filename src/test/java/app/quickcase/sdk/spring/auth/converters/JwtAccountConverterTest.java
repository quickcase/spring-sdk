package app.quickcase.sdk.spring.auth.converters;

import app.quickcase.sdk.spring.auth.OidcConfigDefault;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

class JwtAccountConverterTest {

    private final String CUSTOM_ACCOUNT_CLAIM = "accountClaim";

    private final JwtAccountConverter defaultAccountConverter = new JwtAccountConverter();
    private final JwtAccountConverter customAccountConverter = new JwtAccountConverter(CUSTOM_ACCOUNT_CLAIM);

    @Test
    @DisplayName("should return account claim as string using default claim name")
    void shouldReturnAccountClaimAsStringUsingDefaultClaimName() {
        var jwt = jwtBuilder().claim(OidcConfigDefault.Claims.QC_ACCOUNT, "account-123").build();

        assertThat(defaultAccountConverter.convert(jwt), is("account-123"));
    }

    @Test
    @DisplayName("should return account claim as string when found")
    void shouldReturnAccountClaimAsStringWhenFound() {
        var jwt = jwtBuilder().claim(CUSTOM_ACCOUNT_CLAIM, "account-123").build();

        assertThat(customAccountConverter.convert(jwt), is("account-123"));
    }

    @Test
    @DisplayName("should return null when jwt does not contain account claim")
    void shouldReturnNullWhenJwtDoesNotContainAccountClaim() {
        var jwt = jwtBuilder().build();

        assertThat(customAccountConverter.convert(jwt), is(nullValue()));
    }

    private Jwt.Builder jwtBuilder() {
        return Jwt.withTokenValue("token-value")
                  .header("alg", "none")
                  .claim("claim1", "value1");
    }
}