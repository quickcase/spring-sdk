package app.quickcase.sdk.spring.auth.userinfo;

import java.util.Collections;
import java.util.Map;

import app.quickcase.sdk.spring.auth.OidcException;
import app.quickcase.sdk.spring.auth.claims.ClaimNamesProvider;
import app.quickcase.sdk.spring.auth.claims.ClaimsParser;
import app.quickcase.sdk.spring.auth.organisation.OrganisationProfile;
import app.quickcase.sdk.spring.auth.organisation.OrganisationProfilesParser;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserInfoExtractor {
    private static final OrganisationProfilesParser ORG_PARSER = new OrganisationProfilesParser();
    private static final String ROLES_DELIMITER = ",";
    private static final String GROUPS_DELIMITER = ",";

    private final ClaimNamesProvider claimNames;

    public UserInfoExtractor(ClaimNamesProvider claimNamesProvider) {
        this.claimNames = claimNamesProvider;
    }

    public UserInfo extract(ClaimsParser claimsParser) {
        final String subject = claimsParser.getString(claimNames.sub())
                                           .orElseThrow(() -> new OidcException("Mandatory subject claim missing: " + claimNames.sub()));

        final UserInfo.UserInfoBuilder builder = UserInfo.builder(subject);

        claimsParser.getString(claimNames.name()).ifPresent(builder::name);
        claimsParser.getString(claimNames.email()).ifPresent(builder::email);

        claimsParser.getString(claimNames.roles())
                    .map((str) -> str.split(ROLES_DELIMITER))
                    .ifPresent(builder::authorities);

        claimsParser.getString(claimNames.roles())
                    .map((str) -> str.split(ROLES_DELIMITER))
                    .ifPresent(builder::roles);

        claimsParser.getString(claimNames.groups())
                    .map((str) -> str.split(GROUPS_DELIMITER))
                    .ifPresent(builder::groups);

        return builder.preferences(extractPreferences(claimsParser))
                      .organisationProfiles(extractProfiles(subject, claimsParser))
                      .build();
    }

    private UserPreferences extractPreferences(ClaimsParser claimsParser) {
        final UserPreferences.UserPreferencesBuilder builder = UserPreferences.builder();

        claimsParser.getString(claimNames.defaultJurisdiction()).ifPresent(builder::defaultJurisdiction);
        claimsParser.getString(claimNames.defaultCaseType()).ifPresent(builder::defaultCaseType);
        claimsParser.getString(claimNames.defaultState()).ifPresent(builder::defaultState);

        return builder.build();
    }

    private Map<String, OrganisationProfile> extractProfiles(String subject, ClaimsParser claimsParser) {
        log.debug("Extracting organisation profiles for subject `{}`", subject);
        return claimsParser.getObject(claimNames.organisations())
                           .map(ORG_PARSER::parse)
                           .orElseGet(() -> {
                               log.debug("No organisation profiles extracted for subject `{}`", subject);
                               return Collections.emptyMap();
                           });
    }
}
