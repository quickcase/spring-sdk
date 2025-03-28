package app.quickcase.sdk.spring.auth.converters;

import java.util.Map;
import java.util.Set;

import app.quickcase.sdk.spring.auth.AccessLevel;
import app.quickcase.sdk.spring.auth.SecurityClassification;
import app.quickcase.sdk.spring.auth.claims.ClaimNamesProvider;
import app.quickcase.sdk.spring.auth.organisation.OrganisationProfile;
import app.quickcase.sdk.spring.auth.userinfo.UserInfo;
import app.quickcase.sdk.spring.auth.userinfo.UserPreferences;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JsonUserInfoConverterTest {
    private static final JsonNodeFactory JSON = JsonNodeFactory.instance;

    private static final String SUB_CLAIM = "sub";
    private static final String NAME_CLAIM = "name";
    private static final String EMAIL_CLAIM = "email";
    private static final String ACCOUNT_CLAIM = "app.quickcase.claims/account";
    private static final String ROLES_CLAIM = "app.quickcase.claims/roles";
    private static final String GROUPS_CLAIM = "app.quickcase.claims/groups";
    private static final String DEFAULT_JURISDICTION_CLAIM = "app.quickcase.claims/defaultJurisdiction";
    private static final String DEFAULT_CASE_TYPE_CLAIM = "app.quickcase.claims/defaultCaseType";
    private static final String DEFAULT_STATE_CLAIM = "app.quickcase.claims/defaultState";
    private static final String ORGANISATIONS_CLAIMS = "app.quickcase.claims/orgs";

    @Mock
    private ClaimNamesProvider claimNamesProvider;

    @InjectMocks
    private JsonUserInfoConverter converter;

    @Test
    @DisplayName("should convert JSON to UserInfo")
    void shouldConvertJsonToUserInfo() {
        var json = JSON.objectNode()
                       .put(SUB_CLAIM, "sub-123")
                       .put(NAME_CLAIM, "User 123")
                       .put(EMAIL_CLAIM, "user@quickcase.app")
                       .put(ACCOUNT_CLAIM, "account-123")
                       .put(ROLES_CLAIM, "role1,role2")
                       .put(GROUPS_CLAIM, "group1,group2");

        when(claimNamesProvider.sub()).thenReturn(SUB_CLAIM);
        when(claimNamesProvider.name()).thenReturn(NAME_CLAIM);
        when(claimNamesProvider.email()).thenReturn(EMAIL_CLAIM);
        when(claimNamesProvider.account()).thenReturn(ACCOUNT_CLAIM);
        when(claimNamesProvider.roles()).thenReturn(ROLES_CLAIM);
        when(claimNamesProvider.groups()).thenReturn(GROUPS_CLAIM);

        var userInfo = converter.convert(json);

        assertThat(userInfo, equalTo(
                UserInfo.builder("sub-123")
                        .name("User 123")
                        .email("user@quickcase.app")
                        .account("account-123")
                        .roles(Set.of("role1", "role2"))
                        .groups(Set.of("group1", "group2"))
                        .preferences(UserPreferences.builder().build())
                        .defaultProfile(OrganisationProfile.DEFAULT_USER_PROFILE)
                        .build()
        ));
    }

    @Test
    @DisplayName("should extract preferences")
    void shouldExtractPreferences() {
        var json = JSON.objectNode()
                       .put(SUB_CLAIM, "sub-123")
                       .put(DEFAULT_JURISDICTION_CLAIM, "jurisdiction-123")
                       .put(DEFAULT_CASE_TYPE_CLAIM, "type-123")
                       .put(DEFAULT_STATE_CLAIM, "state-123");

        when(claimNamesProvider.defaultJurisdiction()).thenReturn(DEFAULT_JURISDICTION_CLAIM);
        when(claimNamesProvider.defaultCaseType()).thenReturn(DEFAULT_CASE_TYPE_CLAIM);
        when(claimNamesProvider.defaultState()).thenReturn(DEFAULT_STATE_CLAIM);

        var userInfo = converter.convert(json);

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
        var json = JSON.objectNode()
                       .put(SUB_CLAIM, "sub-123")
                       .put(ORGANISATIONS_CLAIMS, """
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
                               """);

        when(claimNamesProvider.organisations()).thenReturn(ORGANISATIONS_CLAIMS);

        var userInfo = converter.convert(json);

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
}