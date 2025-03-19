package app.quickcase.sdk.spring.auth.userinfo;

import java.util.HashMap;
import java.util.Map;

import app.quickcase.sdk.spring.auth.OidcConfig;
import app.quickcase.sdk.spring.auth.OidcException;
import app.quickcase.sdk.spring.auth.claims.ClaimNamesProvider;
import app.quickcase.sdk.spring.auth.claims.ClaimsParser;
import app.quickcase.sdk.spring.auth.claims.JsonClaimsParser;
import app.quickcase.sdk.spring.auth.organisation.OrganisationProfile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static app.quickcase.sdk.spring.auth.AccessLevel.GROUP;
import static app.quickcase.sdk.spring.auth.AccessLevel.ORGANISATION;
import static app.quickcase.sdk.spring.auth.SecurityClassification.PRIVATE;
import static app.quickcase.sdk.spring.auth.SecurityClassification.PUBLIC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("UserInfoExtractor")
class UserInfoExtractorTest {
    private static final String CLAIM_SUB = "conf-sub";
    private static final String CLAIM_NAME = "conf-name";
    private static final String CLAIM_EMAIL = "conf-email";
    private static final String CLAIM_ROLES = "conf-roles";
    private static final String CLAIM_GROUPS = "conf-groups";
    private static final String CLAIM_ORGS = "conf-orgs";
    private static final String CLAIM_DEF_JURISDICTION = "conf-jurisdiction";
    private static final String CLAIM_DEF_CASE_TYPE = "conf-case-type";
    private static final String CLAIM_DEF_STATE = "conf-state";

    private static final String USER_APP_ROLES = "role1,role2";
    private static final String USER_GROUPS = "group1,group2";
    private static final String USER_ID = "eec55037-bac7-46b4-9849-f063e627e4f3";
    private static final String USER_NAME = "Test User";
    private static final String USER_EMAIL = "test@quickcase.app";
    private static final String DEFAULT_JURISDICTION = "jid1";
    private static final String DEFAULT_CASE_TYPE = "ct1";
    private static final String DEFAULT_STATE = "stateA";
    private static final String USER_ORGANISATIONS = "{" +
            "\"org-1\": {\"access\": \"organisation\", \"classification\": \"private\"}," +
            "\"org-2\": {\"access\": \"group\", \"classification\": \"public\", \"group\": \"group-1\"}" +
            "}";

    @Test
    @DisplayName("should extract userInfo from claims")
    void shouldExtractUserInfo() {
        final UserInfo userInfo = new UserInfoExtractor(claimNamesProvider()).extract(claims());

        assertThat(userInfo, is(notNullValue()));
        assertAll(
                () -> assertThat(userInfo.getSubject(), equalTo(USER_ID)),
                () -> assertThat(userInfo.getName(), equalTo(USER_NAME)),
                () -> assertThat(userInfo.getEmail().orElseThrow(), equalTo(USER_EMAIL)),
                () -> assertThat(userInfo.getAuthorities(), containsInAnyOrder(
                        new SimpleGrantedAuthority("role1"),
                        new SimpleGrantedAuthority("role2")
                )),
                () -> assertThat(userInfo.getRoles(), containsInAnyOrder("role1", "role2")),
                () -> assertThat(userInfo.getGroups(), containsInAnyOrder("group1", "group2"))
        );
    }

    @Test
    @DisplayName("should extract user preferences")
    void shouldExtractUserPreferences() {
        final UserInfo userInfo = new UserInfoExtractor(claimNamesProvider()).extract(claims());
        final UserPreferences preferences = userInfo.getPreferences();

        assertAll(
                () -> assertThat(preferences.getDefaultJurisdiction(), equalTo(DEFAULT_JURISDICTION)),
                () -> assertThat(preferences.getDefaultCaseType(), equalTo(DEFAULT_CASE_TYPE)),
                () -> assertThat(preferences.getDefaultState(), equalTo(DEFAULT_STATE))
        );
    }

    @Test
    @DisplayName("should extract organisation profiles")
    void shouldExtractOrganisationProfiles() {
        final UserInfo userInfo = new UserInfoExtractor(claimNamesProvider()).extract(claims());

        final Map<String, OrganisationProfile> profiles = userInfo.getOrganisationProfiles();
        assertThat(profiles.size(), is(2));

        final OrganisationProfile profile1 = profiles.get("org-1");
        assertAll(
                () -> assertThat(profile1.getAccessLevel(), is(ORGANISATION)),
                () -> assertThat(profile1.getSecurityClassification(), is(PRIVATE)),
                () -> assertThat(profile1.getGroup().isPresent(), is(false))
        );

        final OrganisationProfile profile2 = profiles.get("org-2");
        assertAll(
                () -> assertThat(profile2.getAccessLevel(), is(GROUP)),
                () -> assertThat(profile2.getSecurityClassification(), is(PUBLIC)),
                () -> assertThat(profile2.getGroup().orElse("N/A"), equalTo("group-1"))
        );
    }

    @Test
    @DisplayName("should expect most claims to be optional")
    void shouldExpectClaimsToBeOptional() {
        final UserInfo userInfo = new UserInfoExtractor(claimNamesProvider()).extract(minimumClaims());

        assertThat(userInfo, is(notNullValue()));
        assertAll(
                () -> assertThat(userInfo.getSubject(), equalTo(USER_ID)),
                () -> assertThat(userInfo.getName(), equalTo(USER_ID)),
                () -> assertThat(userInfo.getEmail().isEmpty(), is(true))
        );
    }

    @Test
    @DisplayName("should throw exception when `sub` claim missing")
    void shouldThrowExceptionWhenNoSubClaim() {
        final ClaimsParser emptyClaims = new JsonClaimsParser(new HashMap<>());

        assertThrows(OidcException.class,
                     () -> new UserInfoExtractor(claimNamesProvider()).extract(emptyClaims),
                     "Mandatory 'sub' claim missing");
    }

    private ClaimsParser claims() {
        final Map<String, JsonNode> claims = new HashMap<>();
        claims.put(CLAIM_SUB, textNode(USER_ID));
        claims.put(CLAIM_NAME, textNode(USER_NAME));
        claims.put(CLAIM_EMAIL, textNode(USER_EMAIL));
        claims.put(CLAIM_ROLES, textNode(USER_APP_ROLES));
        claims.put(CLAIM_GROUPS, textNode(USER_GROUPS));
        claims.put(CLAIM_ORGS, textNode(USER_ORGANISATIONS));
        claims.put(CLAIM_DEF_JURISDICTION, textNode(DEFAULT_JURISDICTION));
        claims.put(CLAIM_DEF_CASE_TYPE, textNode(DEFAULT_CASE_TYPE));
        claims.put(CLAIM_DEF_STATE, textNode(DEFAULT_STATE));
        return new JsonClaimsParser(claims);
    }

    private ClaimsParser minimumClaims() {
        final Map<String, JsonNode> claims = new HashMap<>();
        claims.put(CLAIM_SUB, textNode(USER_ID));
        return new JsonClaimsParser(claims);
    }

    private JsonNode textNode(String value) {
        return new TextNode(value);
    }

    private ClaimNamesProvider claimNamesProvider() {
        var claimNames = new OidcConfig.ClaimNames(
                CLAIM_SUB,
                CLAIM_NAME,
                CLAIM_EMAIL,
                CLAIM_ROLES,
                CLAIM_GROUPS,
                CLAIM_ORGS,
                CLAIM_DEF_JURISDICTION,
                CLAIM_DEF_CASE_TYPE,
                CLAIM_DEF_STATE
        );
        return new ClaimNamesProvider(new OidcConfig.Claims("", claimNames));
    }

}