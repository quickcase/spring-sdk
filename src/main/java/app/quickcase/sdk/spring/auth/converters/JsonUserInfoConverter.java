package app.quickcase.sdk.spring.auth.converters;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import app.quickcase.sdk.spring.auth.claims.ClaimNamesProvider;
import app.quickcase.sdk.spring.auth.organisation.OrganisationProfile;
import app.quickcase.sdk.spring.auth.organisation.OrganisationProfilesParser;
import app.quickcase.sdk.spring.auth.userinfo.UserInfo;
import app.quickcase.sdk.spring.auth.userinfo.UserPreferences;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

@Slf4j
public class JsonUserInfoConverter implements Converter<ObjectNode, UserInfo> {
    private static final String EMPTY_STRING = "";
    private static final String COMMA_DELIMITER = ",";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final OrganisationProfilesParser ORG_PARSER = new OrganisationProfilesParser();

    private final ClaimNamesProvider claimNames;

    public JsonUserInfoConverter(ClaimNamesProvider claimNames) {
        this.claimNames = claimNames;
    }

    @NonNull
    @Override
    public UserInfo convert(@NonNull ObjectNode claims) {
        var subject = extractString(claims, claimNames.sub());
        return UserInfo.builder(subject)
                       .name(extractString(claims, claimNames.name()))
                       .email(extractString(claims, claimNames.email()))
                       .account(extractString(claims, claimNames.account()))
                       .roles(extractStrings(claims, claimNames.roles(), COMMA_DELIMITER))
                       .groups(extractStrings(claims, claimNames.groups(), COMMA_DELIMITER))
                       .preferences(extractPreferences(claims))
                       .organisationProfiles(extractProfiles(subject, claims))
                       .defaultProfile(OrganisationProfile.DEFAULT_USER_PROFILE)
                       .build();
    }

    @NonNull
    private Optional<String> extractOptionalString(JsonNode claims, String claimName) {
        if (!claims.has(claimName)) {
            return Optional.empty();
        }

        var valueNode = claims.get(claimName);

        if (!valueNode.isTextual()) {
            return Optional.empty();
        }

        return Optional.of(valueNode.asText());
    }

    @NonNull
    private String extractString(ObjectNode claims, String claimName) {
        return extractOptionalString(claims, claimName).orElse(EMPTY_STRING);
    }

    @NonNull
    private Set<String> extractStrings(ObjectNode claims, String claimName, String delimiter) {
        return Set.of(extractString(claims, claimName).split(delimiter));
    }

    @NonNull
    private UserPreferences extractPreferences(ObjectNode claims) {
        var builder = UserPreferences.builder();

        extractOptionalString(claims, claimNames.defaultJurisdiction()).ifPresent(builder::defaultJurisdiction);
        extractOptionalString(claims, claimNames.defaultCaseType()).ifPresent(builder::defaultCaseType);
        extractOptionalString(claims, claimNames.defaultState()).ifPresent(builder::defaultState);

        return builder.build();
    }

    @NonNull
    private Map<String, OrganisationProfile> extractProfiles(String subject, ObjectNode claims) {
        log.debug("Extracting organisation profiles for subject `{}`", subject);

        var organisationStr = extractOptionalString(claims, claimNames.organisations());

        if (organisationStr.isEmpty()) {
            return Map.of();
        }

        try {
            return ORG_PARSER.parse(MAPPER.readTree(organisationStr.get()));
        } catch (JsonProcessingException e) {
            log.warn(
                    "Failed to parse JSON object for claim `{}`, got: `{}`",
                    claimNames.organisations(),
                    organisationStr.get()
            );
            return Map.of();
        }
    }
}
