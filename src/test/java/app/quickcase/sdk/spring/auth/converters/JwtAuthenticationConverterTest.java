package app.quickcase.sdk.spring.auth.converters;

import java.util.Set;

import app.quickcase.sdk.spring.auth.OidcException;
import app.quickcase.sdk.spring.auth.QuickcaseAuthentication;
import app.quickcase.sdk.spring.auth.userinfo.UserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationConverterTest")
class JwtAuthenticationConverterTest {
    private static final String CLIENT_ID = "client-123";
    private static final String OPENID_SCOPE = "openid";

    @Mock
    private JwtClientIdConverter clientIdConverter;

    @Mock
    private JwtScopesConverter scopesConverter;

    @Mock
    private JwtUserInfoConverter userInfoConverter;

    @Mock
    private JwtClientInfoConverter clientInfoConverter;

    private JwtAuthenticationConverter converter;

    @BeforeEach
    void setUp() {
        converter = new JwtAuthenticationConverter(clientIdConverter, scopesConverter, userInfoConverter, clientInfoConverter, OPENID_SCOPE);
    }

    @Test
    @DisplayName("should reject tokens without scopes")
    void shouldRejectTokensWithoutScopes() {
        var jwt = jwt();

        when(scopesConverter.convert(jwt)).thenReturn(Set.of());

        assertThrows(
                OidcException.class,
                () -> converter.convert(jwt)
        );
    }

    @Test
    @DisplayName("should convert JWT token with user info when OpenID scope present")
    void shouldConvertJwtTokenWithUserInfo() {
        var jwt = jwt();

        var userInfo = UserInfo.builder("sub-123")
                               .roles("role1", "role2")
                               .build();

        when(scopesConverter.convert(jwt)).thenReturn(Set.of(OPENID_SCOPE, "scope1", "scope2"));
        when(clientIdConverter.convert(jwt)).thenReturn(CLIENT_ID);
        when(userInfoConverter.convert(jwt)).thenReturn(userInfo);

        var authentication = converter.convert(jwt);

        assertThat(authentication, equalTo(
                QuickcaseAuthentication.builder(jwt)
                                       .clientId(CLIENT_ID)
                                       .authority("SCOPE_" + OPENID_SCOPE)
                                       .authority("SCOPE_scope1")
                                       .authority("SCOPE_scope2")
                                       .authority("ROLE_role1")
                                       .authority("ROLE_role2")
                                       .userInfo(userInfo)
                                       .build()
        ));
    }

    @Test
    @DisplayName("should convert JWT token with client info when OpenID scope absent")
    void shouldConvertJwtTokenWithClientInfo() {
        var jwt = jwt();

        var userInfo = UserInfo.builder("sub-123")
                               .roles("role1", "role2")
                               .build();

        when(scopesConverter.convert(jwt)).thenReturn(Set.of("scope1", "scope2"));
        when(clientIdConverter.convert(jwt)).thenReturn(CLIENT_ID);
        when(clientInfoConverter.convert(jwt)).thenReturn(userInfo);

        var authentication = converter.convert(jwt);

        assertThat(authentication, equalTo(
                QuickcaseAuthentication.builder(jwt)
                                       .clientId(CLIENT_ID)
                                       .authority("SCOPE_scope1")
                                       .authority("SCOPE_scope2")
                                       .authority("ROLE_role1")
                                       .authority("ROLE_role2")
                                       .userInfo(userInfo)
                                       .build()
        ));
    }

    private Jwt jwt() {
        return Jwt.withTokenValue("token-value")
                  .header("alg", "none")
                  .claim("claim1", "value1")
                  .build();
    }
}