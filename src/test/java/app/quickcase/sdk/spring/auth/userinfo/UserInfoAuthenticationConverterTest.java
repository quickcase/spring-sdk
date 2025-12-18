package app.quickcase.sdk.spring.auth.userinfo;

import java.util.Set;

import app.quickcase.sdk.spring.auth.OidcException;
import app.quickcase.sdk.spring.auth.QuickcaseAuthentication;
import app.quickcase.sdk.spring.auth.converters.JsonUserInfoConverter;
import app.quickcase.sdk.spring.auth.converters.JwtClientIdConverter;
import app.quickcase.sdk.spring.auth.converters.JwtClientInfoConverter;
import app.quickcase.sdk.spring.auth.converters.JwtScopesConverter;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserInfoAuthenticationConverter")
class UserInfoAuthenticationConverterTest {
    private static final JsonNodeFactory JSON = JsonNodeFactory.instance;

    private static final String OPENID_SCOPE = "openid";
    private static final String SUBJECT = "sub-123";
    private static final String ACCESS_TOKEN = "token-123";
    private static final String CLIENT_ID = "clientId";

    @Mock
    private JwtClientIdConverter clientIdConverter;

    @Mock
    private JwtScopesConverter scopesConverter;

    @Mock
    private JwtClientInfoConverter clientInfoConverter;

    @Mock
    private UserInfoGateway userInfoGateway;

    @Mock
    private JsonUserInfoConverter userInfoConverter;

    private UserInfoAuthenticationConverter converter;

    @BeforeEach
    void setUp() {
        converter = new UserInfoAuthenticationConverter(
                clientIdConverter,
                scopesConverter,
                clientInfoConverter,
                OPENID_SCOPE,
                userInfoGateway,
                userInfoConverter
        );
    }

    @Test
    @DisplayName("should convert JWT token with client info when OpenID scope absent")
    void shouldConvertJwtTokenWithClientInfo() {
        var jwt = jwt();

        var userInfo = UserInfo.builder(SUBJECT)
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

    @Test
    @DisplayName("should convert JWT token with fetched JSON user info when OpenID scope present")
    void shouldConvertJwtTokenWithUserInfo() {
        var jwt = jwt();

        var jsonUserInfo = JSON.objectNode();
        var userInfo = UserInfo.builder(SUBJECT)
                               .roles("role1", "role2")
                               .build();

        when(scopesConverter.convert(jwt)).thenReturn(Set.of(OPENID_SCOPE, "scope1", "scope2"));
        when(clientIdConverter.convert(jwt)).thenReturn(CLIENT_ID);
        when(userInfoGateway.getClaims(ACCESS_TOKEN)).thenReturn(jsonUserInfo);
        when(userInfoConverter.convert(jsonUserInfo)).thenReturn(userInfo);

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
    @DisplayName("should reject when token subject does not match fetched user info subject")
    void shouldRejectWhenSubjectDoesNotMatch() {
        var jwt = jwt();

        var jsonUserInfo = JSON.objectNode();
        var userInfo = UserInfo.builder("different-subject") // <-- Non-matching subject
                               .roles("role1", "role2")
                               .build();

        when(scopesConverter.convert(jwt)).thenReturn(Set.of(OPENID_SCOPE, "scope1", "scope2"));
        when(userInfoGateway.getClaims(ACCESS_TOKEN)).thenReturn(jsonUserInfo);
        when(userInfoConverter.convert(jsonUserInfo)).thenReturn(userInfo);

        var exception = assertThrows(
                OidcException.class,
                () -> converter.convert(jwt)
        );
        assertThat(exception.getMessage(), is("User info subject does not match expected subject"));
    }

    private Jwt jwt() {
        return Jwt.withTokenValue(ACCESS_TOKEN)
                  .header("alg", "none")
                  .claim("sub", SUBJECT)
                  .build();
    }
}