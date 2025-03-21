package app.quickcase.sdk.spring.auth;

import java.util.Arrays;
import java.util.Optional;

import app.quickcase.sdk.spring.auth.claims.JwtClaimsParser;
import app.quickcase.sdk.spring.auth.converters.JwtAccountConverter;
import app.quickcase.sdk.spring.auth.converters.JwtClientIdConverter;
import app.quickcase.sdk.spring.auth.userinfo.UserInfo;
import app.quickcase.sdk.spring.auth.userinfo.UserInfoExtractor;
import app.quickcase.sdk.spring.auth.userinfo.UserPreferences;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QuickcaseAuthenticationConverterTest {
    private static final String ACCESS_TOKEN = "token123";
    private static final String SUBJECT = "subject-123";
    private static final String CLIENT_ID = "client-123";
    private static final String ACCOUNT = "account-123";
    private static final String SCOPE_1 = "scope-1";
    private static final String SCOPE_2 = "scope-2";
    private static final String USER_ID = "user-456";
    private static final String USER_NAME = "Johnny Walker";
    private static final String USER_EMAIL = "jw@quickcase.app";
    private static final String ROLE_1 = "role-1";
    private static final String ROLE_2 = "role-2";
    private static final String DEFAULT_JURISDICTION = "org-1";

    private QuickcaseAuthenticationConverter converter;
    private JwtClientIdConverter clientIdConverter;
    private JwtAccountConverter accountConverter;
    private UserInfoExtractor userInfoExtractor;

    @BeforeEach
    void setUp() {
        clientIdConverter = mock(JwtClientIdConverter.class);
        accountConverter = mock(JwtAccountConverter.class);
        userInfoExtractor = mock(UserInfoExtractor.class);

        final UserPreferences preferences = UserPreferences.builder()
                                                           .defaultJurisdiction(DEFAULT_JURISDICTION)
                                                           .build();
        final UserInfo userInfo = UserInfo.builder(USER_ID)
                                          .name(USER_NAME)
                                          .email(USER_EMAIL)
                                          .authorities(ROLE_1, ROLE_2)
                                          .roles(ROLE_1, ROLE_2)
                                          .preferences(preferences)
                                          .build();

        when(userInfoExtractor.extract(ArgumentMatchers.any(JwtClaimsParser.class)))
                .thenReturn(userInfo);

        converter = new QuickcaseAuthenticationConverter(clientIdConverter, accountConverter, userInfoExtractor, OidcConfigDefault.OPENID_SCOPE);
    }

    @Nested
    @DisplayName("when client credentials")
    class WhenClientCredentials {

        Jwt jwt;

        @BeforeEach
        void setUp() {
            jwt = Jwt.withTokenValue(ACCESS_TOKEN)
                     .header("alg", "HS256")
                     .claim("sub", SUBJECT)
                     .claim("scope", scopes(SCOPE_1, SCOPE_2))
                     .claim("client_id", CLIENT_ID)
                     .build();
        }

        @Test
        @DisplayName("should get subject from token")
        void shouldGetSubjectFromToken() {
            final QuickcaseAuthentication authentication = converter.convert(jwt);;

            assertThat(authentication.getId(), equalTo(SUBJECT));
        }

        @Test
        @DisplayName("should get client ID from token")
        void shouldGetClientIdFromToken() {
            when(clientIdConverter.convert(jwt)).thenReturn(CLIENT_ID);

            final QuickcaseAuthentication authentication = converter.convert(jwt);;

            assertThat(authentication.getClientId(), equalTo(Optional.of(CLIENT_ID)));
        }

        @Test
        @DisplayName("should get account from token")
        void shouldGetAccountFromToken() {
            when(accountConverter.convert(jwt)).thenReturn(ACCOUNT);

            var authentication = converter.convert(jwt);

            assertThat(authentication.getAccount(), is(ACCOUNT));
        }

        @Test
        @DisplayName("should use scopes as prefixed authorities")
        void shouldUseScopesAsAuthorities() {
            final QuickcaseAuthentication authentication = converter.convert(jwt);;

            assertThat(authentication.getAuthorities(), containsInAnyOrder(authorities(
                    "SCOPE_" + SCOPE_1,
                    "SCOPE_" + SCOPE_2
            )));
        }

        @Test
        @DisplayName("should use scopes as roles")
        void shouldUseScopesAsRoles() {
            final QuickcaseAuthentication authentication = converter.convert(jwt);;

            assertThat(authentication.getRoles(), containsInAnyOrder(SCOPE_1, SCOPE_2));
        }

        @Test
        @DisplayName("should have original access token")
        void shouldHaveOriginalAccessToken() {
            final QuickcaseAuthentication authentication = converter.convert(jwt);;

            assertThat(authentication.getAccessToken(), equalTo(ACCESS_TOKEN));
        }

        @Test
        @DisplayName("should be client authentication")
        void shouldBeClientAuthentication() {
            final QuickcaseAuthentication authentication = converter.convert(jwt);;

            assertThat(authentication, instanceOf(QuickcaseClientAuthentication.class));
        }
    }

    @Nested
    @DisplayName("when user credentials")
    class WhenUserCredentials {

        @Test
        @DisplayName("should get client ID from token")
        void shouldGetClientIdFromToken() {
            var jwt = userJwt();

            when(clientIdConverter.convert(jwt)).thenReturn(CLIENT_ID);

            var authentication = converter.convert(jwt);

            assertThat(authentication.getClientId(), equalTo(Optional.of(CLIENT_ID)));
        }

        @Test
        @DisplayName("should get account from token")
        void shouldGetAccountFromToken() {
            var jwt = userJwt();

            when(accountConverter.convert(jwt)).thenReturn(ACCOUNT);

            var authentication = converter.convert(jwt);

            assertThat(authentication.getAccount(), is(ACCOUNT));
        }

        @Test
        @DisplayName("should combine prefixed scopes and roles as authorities")
        void shouldUseRolesAsAuthorities() {
            var jwt = userJwt();
            var authentication = converter.convert(jwt);

            assertThat(authentication.getAuthorities(), containsInAnyOrder(authorities(
                    "SCOPE_openid",
                    "SCOPE_" + SCOPE_2,
                    "ROLE_" + ROLE_1,
                    "ROLE_" + ROLE_2
            )));
        }

        @Test
        @DisplayName("should expose user roles")
        void shouldUseScopesAsRoles() {
            var jwt = userJwt();
            var authentication = converter.convert(jwt);

            assertThat(authentication.getRoles(), containsInAnyOrder(ROLE_1, ROLE_2));
        }

        @Test
        @DisplayName("should populate user authentication from access token")
        void shouldGetIdFromUser() {
            var jwt = userJwt();
            var authentication = converter.convert(jwt);

            assertThat(authentication, instanceOf(QuickcaseUserAuthentication.class));
            assertThat(authentication.getAccessToken(), equalTo(ACCESS_TOKEN));
            assertThat(authentication.getId(), equalTo(USER_ID));
            assertThat(authentication.getName(), equalTo(USER_NAME));
            assertThat(authentication.getRoles(), containsInAnyOrder(ROLE_1, ROLE_2));
            assertThat(authentication.getUserInfo()
                                     .orElseThrow()
                                     .getPreferences().getDefaultJurisdiction(), equalTo(DEFAULT_JURISDICTION));
        }

        @Test
        @DisplayName("should accept custom scope for openid")
        void shouldAcceptCustomOpenIdScope() {
            converter = new QuickcaseAuthenticationConverter(clientIdConverter, accountConverter, userInfoExtractor, "custom-openid");

            var jwt = userJwt("custom-openid");
            var authentication = converter.convert(jwt);

            assertThat(authentication, instanceOf(QuickcaseUserAuthentication.class));
        }

        private Jwt userJwt() {
            return userJwt(OidcConfigDefault.OPENID_SCOPE);
        }

        private Jwt userJwt(String openidScope) {
            return Jwt.withTokenValue(ACCESS_TOKEN)
                      .header("alg", "HS256")
                      .claim("scope", scopes(openidScope, SCOPE_2))
                      .claim("client_id", CLIENT_ID)
                      .claim("sub", USER_ID)
                      .claim("name", USER_NAME)
                      .claim("email", USER_EMAIL)
                      .claim("app.quickcase.claims/roles", roles(ROLE_1, ROLE_2))
                      .claim("app.quickcase.claims/default_jurisdiction", DEFAULT_JURISDICTION)
                      .build();
        }
    }

    private String roles(String... items) {
        return String.join(",", items);
    }

    private String scopes(String... items) {
        return String.join(" ", items);
    }

    private GrantedAuthority[] authorities(String ...authorities) {
        return Arrays.stream(authorities)
                     .map(SimpleGrantedAuthority::new)
                     .toArray(GrantedAuthority[]::new);
    }
}