package app.quickcase.sdk.spring.auth.claims;

import app.quickcase.sdk.spring.auth.OidcConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;

class ClaimNamesProviderTest {
    private static final String PREFIX = "prefix:";
    private static final String SUB = "conf-sub";
    private static final String NAME = "conf-name";
    private static final String EMAIL = "conf-email";
    private static final String ROLES = "conf-roles";
    private static final String GROUPS = "conf-groups";
    private static final String ORGS = "conf-orgs";
    private static final String DEF_JURISDICTION = "conf-jurisdiction";
    private static final String DEF_CASE_TYPE = "conf-case-type";
    private static final String DEF_STATE = "conf-state";

    @Test
    @DisplayName("should provide claim names as configured")
    void shouldProvideConfiguredClaims() {
        final OidcConfig.ClaimNames claimNames = new OidcConfig.ClaimNames(SUB,
                                                                           NAME,
                                                                           EMAIL,
                                                                           ROLES,
                                                                           GROUPS,
                                                                           ORGS,
                                                                           DEF_JURISDICTION,
                                                                           DEF_CASE_TYPE,
                                                                           DEF_STATE);
        final OidcConfig.Claims claimsConfig = new OidcConfig.Claims(PREFIX, claimNames);

        final ClaimNamesProvider claimNamesProvider = new ClaimNamesProvider(claimsConfig);

        assertAll(
                () -> assertThat(claimNamesProvider.sub(), equalTo(SUB)),
                () -> assertThat(claimNamesProvider.name(), equalTo(NAME)),
                () -> assertThat(claimNamesProvider.email(), equalTo(EMAIL)),
                () -> assertThat(claimNamesProvider.roles(), equalTo(PREFIX + ROLES)),
                () -> assertThat(claimNamesProvider.groups(), equalTo(PREFIX + GROUPS)),
                () -> assertThat(claimNamesProvider.organisations(), equalTo(PREFIX + ORGS)),
                () -> assertThat(claimNamesProvider.defaultJurisdiction(), equalTo(PREFIX + DEF_JURISDICTION)),
                () -> assertThat(claimNamesProvider.defaultCaseType(), equalTo(PREFIX + DEF_CASE_TYPE)),
                () -> assertThat(claimNamesProvider.defaultState(), equalTo(PREFIX + DEF_STATE))
        );
    }

}