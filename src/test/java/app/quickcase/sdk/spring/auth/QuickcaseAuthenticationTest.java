package app.quickcase.sdk.spring.auth;

import app.quickcase.sdk.spring.auth.organisation.OrganisationProfile;
import app.quickcase.sdk.spring.auth.userinfo.UserInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("QuickcaseAuthentication")
class QuickcaseAuthenticationTest {
    private static final String SUBJECT = "sub-123";

    @Test
    @DisplayName("should expose user info properties for backward compatibility")
    void shouldExposeUserInfoPropertiesForBackwardCompatibility() {
        var jwt = jwt();
        var orgProfile = OrganisationProfile.builder().build();
        var userInfo = UserInfo.builder(SUBJECT)
                               .account("account-123")
                               .roles("role1", "role2")
                               .groups("group1", "group2")
                               .organisationProfile("org1", orgProfile)
                               .build();

        var authentication = QuickcaseAuthentication.builder(jwt)
                                                    .userInfo(userInfo)
                                                    .build();

        assertAll(
                () -> assertThat(authentication.getAccount(), is("account-123")),
                () -> assertThat(authentication.getRoles(), containsInAnyOrder("role1", "role2")),
                () -> assertThat(authentication.getGroups(), containsInAnyOrder("group1", "group2")),
                () -> assertThat(authentication.getOrganisationProfile("org1"), is(orgProfile))
        );
    }

    private Jwt jwt() {
        return Jwt.withTokenValue("token-value")
                  .header("alg", "none")
                  .claim("claim1", "value1")
                  .build();
    }
}