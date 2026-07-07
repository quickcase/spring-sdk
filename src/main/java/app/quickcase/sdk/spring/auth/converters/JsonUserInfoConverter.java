package app.quickcase.sdk.spring.auth.converters;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import app.quickcase.sdk.spring.auth.claims.ClaimNamesProvider;
import app.quickcase.sdk.spring.auth.organisation.OrganisationProfile;
import app.quickcase.sdk.spring.auth.organisation.OrganisationProfilesParser;
import app.quickcase.sdk.spring.auth.userinfo.UserInfo;
import app.quickcase.sdk.spring.auth.userinfo.UserPreferences;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
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
                       .account(
                               extractOptionalString(claims, claimNames.account())
                                       .orElse(null)
                       )
                       .roles(extractStrings(claims, claimNames.roles(), COMMA_DELIMITER))
                       .groups(extractStrings(claims, claimNames.groups(), COMMA_DELIMITER))
                       .preferences(extractPreferences(claims))
                       .organisationProfiles(extractProfiles(subject, claims))
                       .defaultProfile(OrganisationProfile.DEFAULT_USER_PROFILE)
                       .build();
    }

    @NonNull
    private Optional<JsonNode> extractClaim(ObjectNode claims, String claimName) {
        if (!claims.has(claimName)) {
            return Optional.empty();
        }

        return Optional.of(claims.get(claimName));
    }

    @NonNull
    private Optional<String> extractOptionalString(ObjectNode claims, String claimName) {
        return extractClaim(claims, claimName).filter(JsonNode::isTextual)
                                              .map(JsonNode::asText);
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

        return extractClaim(claims, claimNames.organisations()).map(this::parseProfiles)
                                                               .orElse(Map.of());
    }

    @NonNull
    private Map<String, OrganisationProfile> parseProfiles(JsonNode organisationNode) {
        if (organisationNode.isObject()) {
            return ORG_PARSER.parse(organisationNode);
        }

        if (organisationNode.isTextual()) {
            try {
                return ORG_PARSER.parse(MAPPER.readTree(organisationNode.asText()));
            } catch (JacksonException e) {
                log.warn(
                        "Failed to parse organisation profiles from JSON object for claim `{}`",
                        claimNames.organisations(),
                        e
                );
                return Map.of();
            }
        }

        log.warn("Failed to parse organisation profiles: Unsupported JSON node {}", organisationNode);
        return Map.of();
    }

}
