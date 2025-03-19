package app.quickcase.sdk.spring.auth.organisation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import app.quickcase.sdk.spring.auth.AccessLevel;
import app.quickcase.sdk.spring.auth.SecurityClassification;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrganisationProfilesParser {
    private static final String NODE_ACCESS = "access";
    private static final String NODE_CLASSIFICATION = "classification";
    private static final String NODE_GROUP = "group";

    public Map<String, OrganisationProfile> parse(JsonNode tree) {
        if (tree == null || !tree.isObject()) {
            log.warn("Failed to parse organisations: expected object but was {}", tree);
            return Collections.emptyMap();
        }

        final Map<String, OrganisationProfile> profiles = new HashMap<>();
        tree.fieldNames()
            .forEachRemaining(orgId -> toOrganisationProfile(orgId, tree.get(orgId))
                    .ifPresent(profile -> profiles.put(orgId, profile)));

        log.debug("Parsed {} organisation profiles", profiles.size());
        return profiles;
    }

    private Optional<OrganisationProfile> toOrganisationProfile(String orgId, JsonNode node) {
        if (node == null || !node.isObject()) {
            log.warn("Failed to parse organisation `{}`: expected object but was {}", orgId, node);
            return Optional.empty();
        }

        final OrganisationProfile.OrganisationProfileBuilder builder = OrganisationProfile.builder();

        final Optional<AccessLevel> accessLevel = extractAccessLevel(node);
        final Boolean groupEnabled = Optional.of(AccessLevel.GROUP).equals(accessLevel);
        accessLevel.ifPresent(builder::accessLevel);

        extractSecurityClassification(node)
                .ifPresent(builder::securityClassification);
        extractGroup(node, groupEnabled)
                .ifPresent(builder::group);

        return Optional.of(builder.build());
    }

    private Optional<AccessLevel> extractAccessLevel(JsonNode node) {
        final JsonNode accessNode = node.get(NODE_ACCESS);

        if (accessNode == null) {
            log.debug("Access level is null, using default instead");
            return Optional.empty();
        }

        final String rawAccessLevel = accessNode.asText().toUpperCase();

        try {
            return Optional.of(AccessLevel.valueOf(rawAccessLevel));
        } catch (IllegalArgumentException ex) {
            log.warn("Failed to extract malformed access level `" + rawAccessLevel + "`, using default instead", ex);
        }

        return Optional.empty();
    }

    private Optional<SecurityClassification> extractSecurityClassification(JsonNode node) {
        final JsonNode accessNode = node.get(NODE_CLASSIFICATION);

        if (accessNode == null) {
            log.debug("Security classification is null, using default instead");
            return Optional.empty();
        }

        final String rawClassification = accessNode.asText().toUpperCase();

        try {
            return Optional.of(SecurityClassification.valueOf(rawClassification));
        } catch (IllegalArgumentException ex) {
            log.warn("Failed to extract malformed security classification `" + rawClassification + "`, using default instead", ex);
        }

        return Optional.empty();
    }

    private Optional<String> extractGroup(JsonNode node, Boolean groupEnabled) {
        final JsonNode groupNode = node.get(NODE_GROUP);

        if (groupNode == null) {
            if (groupEnabled) {
                log.warn("Group expected but was null");
            }
            return Optional.empty();
        }

        final String group = groupNode.asText().toLowerCase();

        if (!groupEnabled) {
            log.warn("Group not expected but was `{}`, ignoring", group);
            return Optional.empty();
        }

        return Optional.of(group);
    }
}
