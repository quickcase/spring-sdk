package app.quickcase.sdk.spring.auth.converters;

import java.util.Map;
import java.util.Set;

import app.quickcase.sdk.spring.auth.AccessLevel;
import app.quickcase.sdk.spring.auth.SecurityClassification;
import app.quickcase.sdk.spring.auth.claims.ClaimNamesProvider;
import app.quickcase.sdk.spring.auth.organisation.OrganisationProfile;
import app.quickcase.sdk.spring.auth.userinfo.UserInfo;
import app.quickcase.sdk.spring.auth.userinfo.UserPreferences;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtUserInfoConverterTest {
    private static final String NAME_CLAIM = "name";
    private static final String EMAIL_CLAIM = "email";
    private static final String DEFAULT_JURISDICTION_CLAIM = "app.quickcase.claims/defaultJurisdiction";
    private static final String DEFAULT_CASE_TYPE_CLAIM = "app.quickcase.claims/defaultCaseType";
    private static final String DEFAULT_STATE_CLAIM = "app.quickcase.claims/defaultState";
    private static final String ORGANISATIONS_CLAIMS = "app.quickcase.claims/orgs";

    @Mock
    private ClaimNamesProvider claimNamesProvider;

    @Mock
    private JwtAccountConverter accountConverter;

    @Mock
    private JwtRolesConverter rolesConverter;

    @Mock
    private JwtGroupsConverter groupsConverter;

    @InjectMocks
    private JwtUserInfoConverter converter;

    @Test
    @DisplayName("should convert JWT to UserInfo")
    void shouldConvertJwtToClientUserInfo() {
        var jwt = jwtBuilder().claim("sub", "sub-123")
                              .claim(NAME_CLAIM, "User 123")
                              .claim(EMAIL_CLAIM, "user@quickcase.app")
                              .build();

        when(claimNamesProvider.name()).thenReturn(NAME_CLAIM);
        when(claimNamesProvider.email()).thenReturn(EMAIL_CLAIM);
        when(claimNamesProvider.organisations()).thenReturn(ORGANISATIONS_CLAIMS);
        when(claimNamesProvider.defaultJurisdiction()).thenReturn(DEFAULT_JURISDICTION_CLAIM);
        when(claimNamesProvider.defaultCaseType()).thenReturn(DEFAULT_CASE_TYPE_CLAIM);
        when(claimNamesProvider.defaultState()).thenReturn(DEFAULT_STATE_CLAIM);

        when(accountConverter.convert(jwt)).thenReturn("account-123");
        when(rolesConverter.convert(jwt)).thenReturn(Set.of("role1", "role2"));
        when(groupsConverter.convert(jwt)).thenReturn(Set.of("group1", "group2"));

        var userInfo = converter.convert(jwt);

        assertThat(userInfo, equalTo(
                UserInfo.builder("sub-123")
                        .name("User 123")
                        .email("user@quickcase.app")
                        .account("account-123")
                        .roles(Set.of("role1", "role2"))
                        .groups(Set.of("group1", "group2"))
                        .preferences(UserPreferences.builder().build())
                        .organisationProfiles(Map.of())
                        .defaultProfile(OrganisationProfile.DEFAULT_USER_PROFILE)
                        .build())
        );
    }

    @Test
    @DisplayName("should extract preferences")
    void shouldExtractPreferences() {
        var jwt = jwtBuilder().claim("sub", "sub-123")
                              .claim(DEFAULT_JURISDICTION_CLAIM, "jurisdiction-123")
                              .claim(DEFAULT_CASE_TYPE_CLAIM, "type-123")
                              .claim(DEFAULT_STATE_CLAIM, "state-123")
                              .build();

        when(claimNamesProvider.name()).thenReturn(NAME_CLAIM);
        when(claimNamesProvider.email()).thenReturn(EMAIL_CLAIM);
        when(claimNamesProvider.organisations()).thenReturn(ORGANISATIONS_CLAIMS);
        when(claimNamesProvider.defaultJurisdiction()).thenReturn(DEFAULT_JURISDICTION_CLAIM);
        when(claimNamesProvider.defaultCaseType()).thenReturn(DEFAULT_CASE_TYPE_CLAIM);
        when(claimNamesProvider.defaultState()).thenReturn(DEFAULT_STATE_CLAIM);

        var userInfo = converter.convert(jwt);

        assertThat(userInfo.getPreferences(), equalTo(
                UserPreferences.builder()
                               .defaultJurisdiction("jurisdiction-123")
                               .defaultCaseType("type-123")
                               .defaultState("state-123")
                               .build()
        ));
    }

    @Test
    @DisplayName("should extract organisation profiles")
    void shouldExtractOrganisationProfiles() {
        var jwt = jwtBuilder().claim("sub", "sub-123")
                              .claim(ORGANISATIONS_CLAIMS, """
                                      {
                                        "org1": {
                                            "access": "ORGANISATION",
                                            "classification": "PRIVATE"
                                        },
                                        "org2": {
                                            "access": "GROUP",
                                            "group": "group1",
                                            "classification": "PUBLIC"
                                        }
                                      }
                                      """)
                              .build();

        when(claimNamesProvider.name()).thenReturn(NAME_CLAIM);
        when(claimNamesProvider.email()).thenReturn(EMAIL_CLAIM);
        when(claimNamesProvider.organisations()).thenReturn(ORGANISATIONS_CLAIMS);
        when(claimNamesProvider.defaultJurisdiction()).thenReturn(DEFAULT_JURISDICTION_CLAIM);
        when(claimNamesProvider.defaultCaseType()).thenReturn(DEFAULT_CASE_TYPE_CLAIM);
        when(claimNamesProvider.defaultState()).thenReturn(DEFAULT_STATE_CLAIM);

        var userInfo = converter.convert(jwt);

        assertThat(userInfo.getOrganisationProfiles(), equalTo(
                Map.of(
                        "org1", OrganisationProfile.builder()
                                                   .accessLevel(AccessLevel.ORGANISATION)
                                                   .securityClassification(SecurityClassification.PRIVATE)
                                                   .build(),
                        "org2", OrganisationProfile.builder()
                                                   .accessLevel(AccessLevel.GROUP)
                                                   .group("group1")
                                                   .securityClassification(SecurityClassification.PUBLIC)
                                                   .build()
                )
        ));
    }

    private Jwt.Builder jwtBuilder() {
        return Jwt.withTokenValue("token-value")
                  .header("alg", "none")
                  .claim("claim1", "value1");
    }

}