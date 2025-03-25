package app.quickcase.sdk.spring.auth.converters;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import static app.quickcase.sdk.spring.auth.OidcConfigDefault.Claims.QC_GROUPS;
import static app.quickcase.sdk.spring.auth.converters.JwtStringSetConverter.SPACE_DELIMITER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class JwtGroupsConverterTest {

    @Test
    @DisplayName("should return empty set when no jwt groups claim")
    void shouldReturnEmptySetWhenNoJwtGroupsClaim() {
        var jwt = jwt().build();

        var converter = new JwtGroupsConverter();

        assertThat(converter.convert(jwt), is(empty()));
    }

    @Test
    @DisplayName("should return set of parsed groups")
    void shouldReturnSetOfParsedGroups() {
        var jwt = jwt().claim(QC_GROUPS, "group1,group2,group3").build();

        var converter = new JwtGroupsConverter();

        assertThat(converter.convert(jwt), containsInAnyOrder("group1", "group2", "group3"));
    }

    @Test
    @DisplayName("should return set of parsed groups from custom claim")
    void shouldReturnSetOfParsedGroupsFromCustomClaim() {
        var jwt = jwt().claim("customGroups", "group1,group2,group3").build();

        var converter = new JwtGroupsConverter("customGroups");

        assertThat(converter.convert(jwt), containsInAnyOrder("group1", "group2", "group3"));
    }

    @Test
    @DisplayName("should return set of parsed groups with custom delimiter")
    void shouldReturnSetOfParsedGroupsWithCustomDelimiter() {
        var jwt = jwt().claim(QC_GROUPS, "group1 group2 group3").build();

        var converter = new JwtGroupsConverter(QC_GROUPS, SPACE_DELIMITER);

        assertThat(converter.convert(jwt), containsInAnyOrder("group1", "group2", "group3"));
    }

    private Jwt.Builder jwt() {
        return Jwt.withTokenValue("token")
                  .header("alg", "none")
                  .claim("claim1", "value1");
    }
}