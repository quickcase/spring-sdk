package app.quickcase.sdk.spring.auth.userinfo;

import java.util.Arrays;
import java.util.Map;

import app.quickcase.sdk.spring.auth.AccessLevel;
import app.quickcase.sdk.spring.auth.OidcConfigDefault;
import app.quickcase.sdk.spring.auth.QuickcaseAuthentication;
import app.quickcase.sdk.spring.auth.QuickcaseUserAuthentication;
import app.quickcase.sdk.spring.auth.SecurityClassification;
import app.quickcase.sdk.spring.auth.claims.JsonClaimsParser;
import app.quickcase.sdk.spring.auth.converters.JwtAccountConverter;
import app.quickcase.sdk.spring.auth.converters.JwtClientIdConverter;
import app.quickcase.sdk.spring.auth.organisation.OrganisationProfile;
import com.fasterxml.jackson.databind.node.TextNode;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
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

@DisplayName("UserInfoAuthenticationConverter")
class UserInfoAuthenticationConverterTest {
    private static final String ACCESS_TOKEN = "token123";
    private static final String CLIENT_ID = "clientId";
    private static final String SCOPE_2 = "scope-2";
    private static final String USER_ID = "user-456";
    private static final String USER_NAME = "Johnny Walker";
    private static final String USER_EMAIL = "jw@quickcase.app";
    private static final String ROLE_1 = "role-1";
    private static final String ROLE_2 = "role-2";

    private JwtClientIdConverter clientIdConverter;
    private JwtAccountConverter accountConverter;
    private UserInfoGateway userInfoGateway;
    private UserInfoExtractor userInfoExtractor;
    private UserInfoAuthenticationConverter converter;

    @BeforeEach
    void setUp() {
        clientIdConverter = mock(JwtClientIdConverter.class);
        accountConverter = mock(JwtAccountConverter.class);
        userInfoGateway = mock(UserInfoGateway.class);
        userInfoExtractor = mock(UserInfoExtractor.class);

        converter = new UserInfoAuthenticationConverter(
                clientIdConverter,
                accountConverter,
                userInfoGateway,
                userInfoExtractor,
                OidcConfigDefault.OPENID_SCOPE
        );
    }

    @Nested
    @DisplayName("when user credentials")
    class WhenUserCredentials {

        private QuickcaseAuthentication userAuthentication() {
            return userAuthentication(OidcConfigDefault.OPENID_SCOPE);
        }

        private QuickcaseAuthentication userAuthentication(String openidScope) {
            when(userInfoGateway.getClaims(ACCESS_TOKEN)).thenReturn(Map.of(
                    "sub", new TextNode(USER_ID)
            ));

            final OrganisationProfile orgA = OrganisationProfile.builder()
                                                                .accessLevel(AccessLevel.GROUP)
                                                                .securityClassification(SecurityClassification.PRIVATE)
                                                                .group("org-a-group")
                                                                .build();

            when(userInfoExtractor.extract(ArgumentMatchers.any(JsonClaimsParser.class)))
                    .thenReturn(
                            UserInfo.builder(USER_ID)
                                    .name(USER_NAME)
                                    .email(USER_EMAIL)
                                    .roles(ROLE_1, ROLE_2)
                                    .organisationProfile("org-a", orgA)
                                    .build()
                    );

            final Jwt jwt = Jwt.withTokenValue(ACCESS_TOKEN)
                               .header("alg", "HS256")
                               .claim("sub", USER_ID)
                               .claim("scope", scopes(openidScope, SCOPE_2))
                               .claim("client_id", CLIENT_ID)
                               .build();

            return converter.convert(jwt);
        }

        @Test
        @DisplayName("should get ID from user")
        void shouldGetIdFromUser() {
            final QuickcaseAuthentication authentication = userAuthentication();

            assertThat(authentication.getId(), equalTo(USER_ID));
        }

        @Test
        @DisplayName("should use user name")
        void shouldUseUserName() {
            final QuickcaseAuthentication authentication = userAuthentication();

            assertThat(authentication.getName(), equalTo(USER_NAME));
        }

        @Test
        @DisplayName("should have user info")
        void shouldHaveUserInfo() {
            final QuickcaseAuthentication authentication = userAuthentication();

            assertThat(authentication.getUserInfo()
                                     .orElseThrow()
                                     .getOrganisationProfiles(), aMapWithSize(1));
        }

        @Test
        @DisplayName("should combine prefixed scopes and roles as authorities")
        void shouldUseRolesAsAuthorities() {
            final QuickcaseAuthentication authentication = userAuthentication();

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
            final QuickcaseAuthentication authentication = userAuthentication();

            assertThat(authentication.getRoles(), containsInAnyOrder(ROLE_1, ROLE_2));
        }

        @Test
        @DisplayName("should have original access token")
        void shouldHaveOriginalAccessToken() {
            final QuickcaseAuthentication authentication = userAuthentication();

            assertThat(authentication.getAccessToken(), equalTo(ACCESS_TOKEN));
        }

        @Test
        @DisplayName("should be user authentication")
        void shouldBeUserAuthentication() {
            final QuickcaseAuthentication authentication = userAuthentication();

            assertThat(authentication, instanceOf(QuickcaseUserAuthentication.class));
        }

        @Test
        @DisplayName("should extract organisation profiles")
        void shouldExtractOrganisationProfiles() {
            final QuickcaseAuthentication authentication = userAuthentication();

            final OrganisationProfile profile = authentication.getOrganisationProfile("org-a");

            Assertions.assertAll(
                    () -> assertThat(profile.getAccessLevel(), Matchers.is(AccessLevel.GROUP)),
                    () -> assertThat(profile.getSecurityClassification(), Matchers.is(SecurityClassification.PRIVATE)),
                    () -> assertThat(profile.getGroup().orElse("N/A"), equalTo("org-a-group"))
            );
        }

        @Test
        @DisplayName("should accept custom scope for openid")
        void shouldAcceptCustomOpenIdScope() {
            converter = new UserInfoAuthenticationConverter(clientIdConverter, accountConverter, userInfoGateway, userInfoExtractor, "custom-openid");

            final QuickcaseAuthentication authentication = userAuthentication("custom-openid");

            assertThat(authentication, instanceOf(QuickcaseUserAuthentication.class));
        }
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