package app.quickcase.sdk.spring.auth.organisation;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static app.quickcase.sdk.spring.auth.AccessLevel.*;
import static app.quickcase.sdk.spring.auth.SecurityClassification.PRIVATE;
import static app.quickcase.sdk.spring.auth.SecurityClassification.PUBLIC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

class OrganisationProfilesParserTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final JsonNode ORGANISATIONS_JSON;

    static {
        try {
            ORGANISATIONS_JSON = MAPPER.readTree("""
                    {
                        "org-1": {"access": "organisation", "classification": "private"},
                        "org-2": {"access": "group", "classification": "public", "group": "group-1"}
                    }
                    """);
        } catch (JsonProcessingException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    @DisplayName("should parse JSON payload")
    void shouldParseJson() {
        final Map<String, OrganisationProfile> orgs = new OrganisationProfilesParser().parse(ORGANISATIONS_JSON);

        assertAll(
                () -> assertThat("keys", orgs.keySet(), containsInAnyOrder("org-1", "org-2")),
                () -> assertThat("org-1", orgs.get("org-1"), equalTo(OrganisationProfile.builder()
                                                                                        .accessLevel(ORGANISATION)
                                                                                        .securityClassification(PRIVATE)
                                                                                        .build())),
                () -> assertThat("org-2", orgs.get("org-2"), equalTo(OrganisationProfile.builder()
                                                                                        .accessLevel(GROUP)
                                                                                        .securityClassification(PUBLIC)
                                                                                        .group("group-1")
                                                                                        .build()))
        );
    }

    @Test
    @DisplayName("should return empty map when JSON cannot be parsed")
    void shouldFailSafelyWithEmptyMap() {
        final OrganisationProfilesParser parser = new OrganisationProfilesParser();

        assertAll(
                () -> assertThat("null", parser.parse(null), is(Collections.emptyMap())),
                () -> assertThat("null", parser.parse(new TextNode("hello")), is(Collections.emptyMap()))
        );
    }

    @Test
    @DisplayName("should ignore organisation profile which cannot be parsed")
    void shouldIgnoreInvalidOrganisations() throws JsonProcessingException {
        final JsonNode tree = MAPPER.readTree("""
                {
                    "org-1": {"access": "organisation", "classification": "private"},
                    "org-2": 42
                }
                """);
        final Map<String, OrganisationProfile> orgs = new OrganisationProfilesParser().parse(tree);

        assertThat(orgs.keySet(), contains("org-1"));
    }

    @Test
    @DisplayName("should use default access level when not defined")
    void shouldDefaultNullAccessLevel() throws JsonProcessingException {
        final JsonNode tree = MAPPER.readTree("""
                {
                    "org-1": {}
                }
                """);

        final Map<String, OrganisationProfile> orgs = new OrganisationProfilesParser().parse(tree);

        assertAll(
                () -> assertThat(orgs.keySet(), contains("org-1")),
                () -> assertThat(orgs.get("org-1").getAccessLevel(), equalTo(INDIVIDUAL))
        );
    }

    @Test
    @DisplayName("should use default access level when malformed")
    void shouldDefaultMalformedAccessLevel() throws JsonProcessingException {
        final JsonNode tree = MAPPER.readTree("""
                {
                    "org-1": {"access": "malformed"}
                }
                """);

        final Map<String, OrganisationProfile> orgs = new OrganisationProfilesParser().parse(tree);

        assertAll(
                () -> assertThat(orgs.keySet(), contains("org-1")),
                () -> assertThat(orgs.get("org-1").getAccessLevel(), equalTo(INDIVIDUAL))
        );
    }

    @Test
    @DisplayName("should use default classification when not defined")
    void shouldDefaultNullSecurityClassification() throws JsonProcessingException {
        final JsonNode tree = MAPPER.readTree("""
                {
                    "org-1": {}
                }
                """);

        final Map<String, OrganisationProfile> orgs = new OrganisationProfilesParser().parse(tree);

        assertAll(
                () -> assertThat(orgs.keySet(), contains("org-1")),
                () -> assertThat(orgs.get("org-1").getSecurityClassification(), equalTo(PUBLIC))
        );
    }

    @Test
    @DisplayName("should use default classification when malformed")
    void shouldDefaultMalformedSecurityClassification() throws JsonProcessingException {
        final JsonNode tree = MAPPER.readTree("""
                {
                    "org-1": {"classification": "malformed"}
                }
                """);

        final Map<String, OrganisationProfile> orgs = new OrganisationProfilesParser().parse(tree);

        assertAll(
                () -> assertThat(orgs.keySet(), contains("org-1")),
                () -> assertThat(orgs.get("org-1").getSecurityClassification(), equalTo(PUBLIC))
        );
    }

    @Test
    @DisplayName("should ignore group when access level is not GROUP")
    void shouldIgnoreGroupWhenNotExpected() throws JsonProcessingException {
        final JsonNode tree = MAPPER.readTree("""
                {
                    "org-1": {"access": "INDIVIDUAL", "group": "aGroup"}
                }
                """);

        final Map<String, OrganisationProfile> orgs = new OrganisationProfilesParser().parse(tree);

        assertAll(
                () -> assertThat(orgs.keySet(), contains("org-1")),
                () -> assertThat(orgs.get("org-1").getGroup(), is(Optional.empty()))
        );
    }

    @Test
    @DisplayName("should return empty group when null but expected")
    void shouldReturnEmptyGroupWhenNullButExpected() throws JsonProcessingException {
        final JsonNode tree = MAPPER.readTree("""
                {
                    "org-1": {"access": "GROUP"}
                }
                """);

        final Map<String, OrganisationProfile> orgs = new OrganisationProfilesParser().parse(tree);

        assertAll(
                () -> assertThat(orgs.keySet(), contains("org-1")),
                () -> assertThat(orgs.get("org-1").getGroup(), is(Optional.empty()))
        );
    }
}