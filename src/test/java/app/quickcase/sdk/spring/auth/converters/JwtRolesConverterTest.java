package app.quickcase.sdk.spring.auth.converters;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import static app.quickcase.sdk.spring.auth.OidcConfigDefault.Claims.QC_ROLES;
import static app.quickcase.sdk.spring.auth.converters.JwtStringSetConverter.COMMA_DELIMITER;
import static app.quickcase.sdk.spring.auth.converters.JwtStringSetConverter.SPACE_DELIMITER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class JwtRolesConverterTest {

    @Test
    @DisplayName("should return empty set when no jwt roles claim")
    void shouldReturnEmptySetWhenNoJwtRolesClaim() {
        var jwt = jwt().build();

        var converter = new JwtRolesConverter();

        assertThat(converter.convert(jwt), is(empty()));
    }

    @Test
    @DisplayName("should return set of parsed roles")
    void shouldReturnSetOfParsedRoles() {
        var jwt = jwt().claim(QC_ROLES, "role1,role2,role3").build();

        var converter = new JwtRolesConverter();

        assertThat(converter.convert(jwt), containsInAnyOrder("role1", "role2", "role3"));
    }

    @Test
    @DisplayName("should return set of parsed roles from custom claim")
    void shouldReturnSetOfParsedRolesFromCustomClaim() {
        var jwt = jwt().claim("customRoles", "role1,role2,role3").build();

        var converter = new JwtRolesConverter("customRoles");

        assertThat(converter.convert(jwt), containsInAnyOrder("role1", "role2", "role3"));
    }

    @Test
    @DisplayName("should return set of parsed roles with custom delimiter")
    void shouldReturnSetOfParsedRolesWithCustomDelimiter() {
        var jwt = jwt().claim(QC_ROLES, "role1 role2 role3").build();

        var converter = new JwtRolesConverter(QC_ROLES, SPACE_DELIMITER);

        assertThat(converter.convert(jwt), containsInAnyOrder("role1", "role2", "role3"));
    }

    private Jwt.Builder jwt() {
        return Jwt.withTokenValue("token")
                  .header("alg", "none")
                  .claim("claim1", "value1");
    }
}