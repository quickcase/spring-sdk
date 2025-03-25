package app.quickcase.sdk.spring.auth.converters;

import java.util.Set;

import app.quickcase.sdk.spring.auth.claims.ClaimNamesProvider;
import app.quickcase.sdk.spring.auth.organisation.OrganisationProfile;
import app.quickcase.sdk.spring.auth.userinfo.UserInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtClientInfoConverterTest {
    private static final String NAME_CLAIM = "name";

    @Mock
    private ClaimNamesProvider claimNamesProvider;

    @Mock
    private JwtAccountConverter accountConverter;

    @Mock
    private JwtScopesConverter scopesConverter;

    @Mock
    private JwtRolesConverter rolesConverter;

    @Mock
    private JwtGroupsConverter groupsConverter;

    @InjectMocks
    private JwtClientInfoConverter converter;

    @Test
    @DisplayName("should convert JWT to client UserInfo")
    void shouldConvertJwtToClientUserInfo() {
        var jwt = jwtBuilder().claim("sub", "sub-123")
                              .claim(NAME_CLAIM, "Client 123")
                              .build();

        when(claimNamesProvider.name()).thenReturn(NAME_CLAIM);
        when(accountConverter.convert(jwt)).thenReturn("account-123");
        when(rolesConverter.convert(jwt)).thenReturn(Set.of("role1", "role2"));
        when(groupsConverter.convert(jwt)).thenReturn(Set.of("group1", "group2"));

        var userInfo = converter.convert(jwt);

        assertThat(userInfo, equalTo(
                UserInfo.builder("sub-123")
                        .name("Client 123")
                        .account("account-123")
                        .roles(Set.of("role1", "role2"))
                        .groups(Set.of("group1", "group2"))
                        .defaultProfile(OrganisationProfile.DEFAULT_CLIENT_PROFILE)
                        .build())
        );
    }

    @Test
    @DisplayName("should default name when not defined")
    void shouldDefaultNameWhenNotDefined() {
        var jwt = jwtBuilder().claim("sub", "sub-123").build();

        when(claimNamesProvider.name()).thenReturn(NAME_CLAIM);

        var userInfo = converter.convert(jwt);

        assertThat(userInfo.getName(), is("System"));
    }

    @Test
    @DisplayName("should default roles to scopes when empty")
    void shouldDefaultRolesToScopesWhenEmpty() {
        var jwt = jwtBuilder().claim("sub", "sub-123").build();

        when(claimNamesProvider.name()).thenReturn(NAME_CLAIM);
        when(rolesConverter.convert(jwt)).thenReturn(Set.of());
        when(scopesConverter.convert(jwt)).thenReturn(Set.of("scope1", "scope2"));

        var userInfo = converter.convert(jwt);

        assertThat(userInfo.getRoles(), containsInAnyOrder("scope1", "scope2"));
    }

    private Jwt.Builder jwtBuilder() {
        return Jwt.withTokenValue("token-value")
                  .header("alg", "none")
                  .claim("claim1", "value1");
    }

}