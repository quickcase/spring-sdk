package app.quickcase.sdk.spring.auth.converters;

import java.util.Map;

import app.quickcase.sdk.spring.auth.claims.ClaimNamesProvider;
import app.quickcase.sdk.spring.auth.organisation.OrganisationProfile;
import app.quickcase.sdk.spring.auth.organisation.OrganisationProfilesParser;
import app.quickcase.sdk.spring.auth.userinfo.UserInfo;
import app.quickcase.sdk.spring.auth.userinfo.UserPreferences;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.jwt.Jwt;

@Slf4j
public class JwtUserInfoConverter implements Converter<Jwt, UserInfo> {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final OrganisationProfilesParser ORG_PARSER = new OrganisationProfilesParser();

    private final ClaimNamesProvider claimNames;
    private final JwtAccountConverter accountConverter;
    private final JwtRolesConverter rolesConverter;
    private final JwtGroupsConverter groupsConverter;

    public JwtUserInfoConverter(
            ClaimNamesProvider claimNames,
            JwtAccountConverter accountConverter,
            JwtRolesConverter rolesConverter,
            JwtGroupsConverter groupsConverter
    ) {
        this.claimNames = claimNames;
        this.accountConverter = accountConverter;
        this.rolesConverter = rolesConverter;
        this.groupsConverter = groupsConverter;
    }

    @NonNull
    @Override
    public UserInfo convert(@NonNull Jwt jwt) {
        return UserInfo.builder(jwt.getSubject())
                       .name(jwt.getClaimAsString(claimNames.name()))
                       .email(jwt.getClaimAsString(claimNames.email()))
                       .account(accountConverter.convert(jwt))
                       .roles(rolesConverter.convert(jwt))
                       .groups(groupsConverter.convert(jwt))
                       .preferences(convertPreferences(jwt))
                       .organisationProfiles(convertOrganisationProfiles(jwt))
                       .defaultProfile(OrganisationProfile.DEFAULT_USER_PROFILE)
                       .build();
    }

    private UserPreferences convertPreferences(Jwt jwt) {
        return UserPreferences.builder()
                              .defaultJurisdiction(jwt.getClaimAsString(claimNames.defaultJurisdiction()))
                              .defaultCaseType(jwt.getClaimAsString(claimNames.defaultCaseType()))
                              .defaultState(jwt.getClaimAsString(claimNames.defaultState()))
                              .build();
    }

    private Map<String, OrganisationProfile> convertOrganisationProfiles(Jwt jwt) {
        log.debug("Extracting organisation profiles for subject `{}`", jwt.getSubject());
        var organisationStr = jwt.getClaimAsString(claimNames.organisations());

        if (organisationStr == null) {
            return Map.of();
        }

        try {
            return ORG_PARSER.parse(MAPPER.readTree(organisationStr));
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse JSON object for claim `{}`, got: `{}`", claimNames.organisations(), organisationStr);
            return Map.of();
        }
    }
}
