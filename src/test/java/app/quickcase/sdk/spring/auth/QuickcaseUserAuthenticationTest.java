package app.quickcase.sdk.spring.auth;

import java.util.Optional;
import java.util.Set;

import app.quickcase.sdk.spring.auth.organisation.OrganisationProfile;
import app.quickcase.sdk.spring.auth.userinfo.UserInfo;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QuickcaseUserAuthenticationTest {
    private static final String ACCESS_TOKEN = "access-token-123";
    private static final String CLIENT_ID = "client-123";
    private static final String USER_ID = "user-123";
    private static final String USER_EMAIL = "test@test";
    private static final String USER_NAME = "Jean Paul";

    private static final Jwt JWT = Jwt.withTokenValue(ACCESS_TOKEN)
                                      .header("alg", "HS256")
                                      .claim("sub", USER_ID)
                                      .claim("scope", "SCOPE-1 SCOPE-2")
                                      .claim("client_id", USER_ID)
                                      .build();

    @Test
    @DisplayName("should enforce non-null fields")
    void shouldEnforceNonNullFields() {
        Assertions.assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> new QuickcaseUserAuthentication(null, Set.of(), UserInfo.builder(USER_ID).build(), CLIENT_ID)),
                () -> assertThrows(NullPointerException.class, () -> new QuickcaseUserAuthentication(JWT, null, UserInfo.builder(USER_ID).build(), CLIENT_ID)),
                () -> assertThrows(NullPointerException.class, () -> new QuickcaseUserAuthentication(JWT, Set.of(), null, CLIENT_ID))
        );
    }

    @Test
    @DisplayName("should use user ID as identifier")
    void getId() {
        final QuickcaseAuthentication auth = userAuthentication();
        assertThat(auth.getId(), equalTo(USER_ID));
    }

    @Test
    @DisplayName("should have optional client ID")
    void getClientId() {
        final QuickcaseAuthentication auth = userAuthentication();
        assertThat(auth.getClientId(), equalTo(Optional.of(CLIENT_ID)));
    }

    @Test
    @DisplayName("should have user info")
    void getUserInfo() {
        final QuickcaseAuthentication auth = userAuthentication();

        Assertions.assertAll(
                () -> assertThat(auth.getUserInfo().isPresent(), is(true)),
                () -> assertThat(auth.getUserInfo().flatMap(UserInfo::getEmail), equalTo(Optional.of(USER_EMAIL)))
        );
    }

    @Test
    @DisplayName("should be flagged as authenticated")
    void isAuthenticated() {
        final QuickcaseAuthentication auth = userAuthentication();
        assertThat(auth.isAuthenticated(), is(true));
    }

    @Test
    @DisplayName("should use access token as credentials")
    void getCredentials() {
        final QuickcaseAuthentication auth = userAuthentication();
        assertThat(auth.getCredentials(), is(JWT));
    }

    @Test
    @DisplayName("should have access token")
    void getAccessToken() {
        final QuickcaseAuthentication auth = userAuthentication();
        assertThat(auth.getAccessToken(), equalTo(ACCESS_TOKEN));
    }

    @Test
    @DisplayName("should have authorities")
    void getAuthorities() {
        final QuickcaseAuthentication auth = userAuthentication();
        assertThat(auth.getAuthorities(), containsInAnyOrder(
                new SimpleGrantedAuthority("SCOPE-1"),
                new SimpleGrantedAuthority("SCOPE-2")
        ));
    }

    @Test
    @DisplayName("should use user name")
    void getName() {
        final QuickcaseAuthentication auth = userAuthentication();
        assertThat(auth.getName(), equalTo(USER_NAME));
    }

    @Test
    @DisplayName("should use token as principal")
    void getPrincipal() {
        final QuickcaseAuthentication auth = userAuthentication();
        assertThat(auth.getPrincipal(), is(JWT));
    }

    @Test
    @DisplayName("should have roles")
    void getRoles() {
        final QuickcaseAuthentication auth = userAuthentication();
        assertThat(auth.getRoles(), containsInAnyOrder("role1", "role2"));
    }

    @Test
    @DisplayName("should have groups")
    void getGroups() {
        final QuickcaseAuthentication auth = userAuthentication();
        assertThat(auth.getGroups(), containsInAnyOrder("group1", "group2"));
    }

    @Test
    @DisplayName("should give default organisation profile when org not found")
    void getOrganisationProfileWhenNotFound() {
        final QuickcaseAuthentication auth = userAuthentication();
        final OrganisationProfile orgProfile = auth.getOrganisationProfile("anyOrg");

        assertAll(
                () -> assertThat(orgProfile.getAccessLevel(), Matchers.is(AccessLevel.INDIVIDUAL)),
                () -> assertThat(orgProfile.getSecurityClassification(),
                                 Matchers.is(SecurityClassification.PUBLIC)),
                () -> assertThat(orgProfile.getGroup().isPresent(), is(false))
        );
    }

    @Test
    @DisplayName("should give organisation profile when found")
    void getOrganisationProfileWhenFound() {
        final QuickcaseAuthentication auth = userAuthentication();
        final OrganisationProfile orgProfile = auth.getOrganisationProfile("org-1");

        assertAll(
                () -> assertThat(orgProfile.getAccessLevel(), Matchers.is(AccessLevel.GROUP)),
                () -> assertThat(orgProfile.getSecurityClassification(),
                                 Matchers.is(SecurityClassification.PRIVATE)),
                () -> assertThat(orgProfile.getGroup().orElseThrow(), equalTo("org-1-group"))
        );
    }

    private QuickcaseAuthentication userAuthentication() {
        final Set<GrantedAuthority> authorities = Set.of(
                new SimpleGrantedAuthority("SCOPE-1"),
                new SimpleGrantedAuthority("SCOPE-2")
        );

        final OrganisationProfile profile = OrganisationProfile.builder()
                                                               .accessLevel(AccessLevel.GROUP)
                                                               .group("org-1-group")
                                                               .securityClassification(SecurityClassification.PRIVATE)
                                                               .build();
        final UserInfo userInfo = UserInfo.builder(USER_ID)
                                          .name(USER_NAME)
                                          .email(USER_EMAIL)
                                          .authorities("ROLE-1", "ROLE-2")
                                          .roles("role1", "role2")
                                          .groups("group1", "group2")
                                          .organisationProfile("org-1", profile)
                                          .build();

        return new QuickcaseUserAuthentication(JWT, authorities, userInfo, CLIENT_ID);
    }
}