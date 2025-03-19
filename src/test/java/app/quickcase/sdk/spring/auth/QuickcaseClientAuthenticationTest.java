package app.quickcase.sdk.spring.auth;

import java.util.Set;

import app.quickcase.sdk.spring.auth.organisation.OrganisationProfile;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import static java.util.stream.Collectors.toSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

class QuickcaseClientAuthenticationTest {
    private static final String ACCESS_TOKEN = "access-token-123";
    private static final String CLIENT_ID = "client-123";

    private static final Jwt JWT = Jwt.withTokenValue(ACCESS_TOKEN)
                                      .header("alg", "HS256")
                                      .claim("sub", CLIENT_ID)
                                      .claim("scope", "SCOPE-1 SCOPE-2")
                                      .claim("client_id", CLIENT_ID)
                                      .build();

    @Test
    @DisplayName("should use OAuth2 client ID as identifier")
    void getId() {
        final QuickcaseAuthentication auth = clientAuthentication();
        assertThat(auth.getId(), equalTo(CLIENT_ID));
    }

    @Test
    @DisplayName("should expose authorities as roles")
    void getRoles() {
        final QuickcaseAuthentication auth = clientAuthentication();
        assertThat(auth.getRoles(), containsInAnyOrder("ROLE-1", "ROLE-2"));
    }

    @Test
    @DisplayName("should not have groups")
    void getGroups() {
        final QuickcaseAuthentication auth = clientAuthentication();
        assertThat(auth.getGroups(), is(emptyCollectionOf(String.class)));
    }

    @Test
    @DisplayName("should not have user info")
    void getUserInfo() {
        final QuickcaseAuthentication auth = clientAuthentication();
        assertThat(auth.getUserInfo().isPresent(), is(false));
    }

    @Test
    @DisplayName("should be flagged as authenticated")
    void isAuthenticated() {
        final QuickcaseAuthentication auth = clientAuthentication();
        assertThat(auth.isAuthenticated(), is(true));
    }

    @Test
    @DisplayName("should use access token as credentials")
    void getCredentials() {
        final QuickcaseAuthentication auth = clientAuthentication();
        assertThat(auth.getCredentials(), is(JWT));
    }

    @Test
    @DisplayName("should have access token")
    void getAccessToken() {
        final QuickcaseAuthentication auth = clientAuthentication();
        assertThat(auth.getAccessToken(), equalTo(ACCESS_TOKEN));
    }

    @Test
    @DisplayName("should use default name")
    void getName() {
        final QuickcaseAuthentication auth = clientAuthentication();
        assertThat(auth.getName(), equalTo("System"));
    }

    @Test
    @DisplayName("should use token as principal")
    void getPrincipal() {
        final QuickcaseAuthentication auth = clientAuthentication();
        assertThat(auth.getPrincipal(), is(JWT));
    }

    @Test
    @DisplayName("should always give client's default organisation profile")
    void getOrganisationProfile() {
        final QuickcaseAuthentication auth = clientAuthentication();
        final OrganisationProfile orgProfile = auth.getOrganisationProfile("anyOrg");

        assertAll(
                () -> assertThat(orgProfile.getAccessLevel(), Matchers.is(AccessLevel.ORGANISATION)),
                () -> assertThat(orgProfile.getSecurityClassification(),
                                 Matchers.is(SecurityClassification.PUBLIC)),
                () -> assertThat(orgProfile.getGroup().isPresent(), is(false))
        );
    }

    private QuickcaseAuthentication clientAuthentication() {
        final Set<String> scopes = Set.of("ROLE-1", "ROLE-2");
        final Set<GrantedAuthority> authorities = scopes.stream().map(SimpleGrantedAuthority::new).collect(toSet());
        return new QuickcaseClientAuthentication(JWT, CLIENT_ID, authorities, scopes);
    }
}