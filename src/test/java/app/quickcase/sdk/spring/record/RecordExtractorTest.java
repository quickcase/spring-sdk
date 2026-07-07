package app.quickcase.sdk.spring.record;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecordExtractorTest {
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final String WORKSPACE = "workspace-1";
    private static final String TYPE = "type-1";
    private static final String REFERENCE = "1111222233334444";
    private static final String STATE = "in-progress";
    private static final LocalDateTime CREATED = LocalDateTime.of(2023, Month.FEBRUARY, 14, 23, 11);
    private static final LocalDateTime MODIFIED = LocalDateTime.of(2023, Month.MARCH, 11, 11, 23);
    private static final String CLASSIFICATION = "PRIVATE";
    private static final String DATA = """
            {
                "textField": "value 1",
                "nullField": null,
                "complexField": {
                    "level1": {
                        "level2": "nested value 2"
                    },
                    "nestedSimpleCollection": [
                        {"id": "123", "value": "Collection value 1"},
                        {"id": "456", "value": "Collection value 2"},
                        {"id": "789", "value": "Collection value 3"}
                    ]
                }
            }
            """;

    @Test
    @DisplayName("should throw error when provided record is null")
    void nullRecord() {
        assertThrows(NullPointerException.class, () -> new RecordExtractor(null));
    }

    @Test
    @DisplayName("should throw error when provided path is null")
    void nullPath() {
        final RecordExtractor extractor = new RecordExtractor(record());

        assertThrows(NullPointerException.class, () -> extractor.extract(null));
    }

    @Test
    @DisplayName("should return empty optional when field does not exist")
    void notFound() {
        final RecordExtractor extractor = new RecordExtractor(record());
        final Optional<JsonNode> value = extractor.extract("notAField");

        assertTrue(value.isEmpty(), "Optional#isEmpty()");
    }

    @Test
    @DisplayName("should return empty optional when field exist but is `null`")
    void nullField() {
        final RecordExtractor extractor = new RecordExtractor(record());
        final Optional<JsonNode> value = extractor.extract("nullField");

        assertTrue(value.isEmpty(), "Optional#isEmpty()");
    }

    @Test
    @DisplayName("should extract top-level field from record data")
    void topLevelField() {
        final RecordExtractor extractor = new RecordExtractor(record());
        final Optional<JsonNode> value = extractor.extract("textField");

        assertAll(
                () -> assertTrue(value.isPresent(), "Optional#isPresent()"),
                () -> assertThat(value.get(), equalTo(new StringNode("value 1")))
        );
    }

    @Nested
    @DisplayName("when metadata")
    class Metadata {
        @Test
        @DisplayName("should return empty optional when metadata does not exist")
        void invalidMetadata() {
            final RecordExtractor extractor = new RecordExtractor(record());
            final Optional<JsonNode> value = extractor.extract("[not_a_metadata]");

            assertTrue(value.isEmpty(), "Optional#isEmpty()");
        }

        @DisplayName("when missing metadata delimiters")
        @ParameterizedTest(name = "should extract field with id: {0}")
        @ValueSource(strings = {"workspace", "type", "state"})
        void extractFieldInsteadOfMetadata(String path) {
            final RecordExtractor extractor = new RecordExtractor(record());
            final Optional<JsonNode> value = extractor.extract(path);

            assertTrue(value.isEmpty(), "Optional#isEmpty()");
        }

        @DisplayName("[workspace]")
        @ParameterizedTest(name = "should extract for path: {0}")
        @ValueSource(strings = {
                "[workspace]",
                "[WORKSPACE]", // Case insensitive
                "[organisation]", // Legacy alias
                "[ORGANISATION]", // Legacy alias, case insensitive
                "[jurisdiction]", // Legacy alias
                "[JURISDICTION]", // Legacy alias, case insensitive
        })
        void extractWorkspace(String path) {
            final RecordExtractor extractor = new RecordExtractor(record());
            final Optional<JsonNode> value = extractor.extract(path);

            assertAll(
                    () -> assertTrue(value.isPresent(), "Optional#isPresent()"),
                    () -> assertThat(value.get(), equalTo(new StringNode(WORKSPACE)))
            );
        }

        @DisplayName("[type]")
        @ParameterizedTest(name = "should extract for path: {0}")
        @ValueSource(strings = {
                "[type]",
                "[TYPE]", // Case insensitive
                "[case_type]", // Legacy alias
                "[CASE_TYPE]", // Legacy alias, case insensitive
        })
        void extractType(String path) {
            final RecordExtractor extractor = new RecordExtractor(record());
            final Optional<JsonNode> value = extractor.extract(path);

            assertAll(
                    () -> assertTrue(value.isPresent(), "Optional#isPresent()"),
                    () -> assertThat(value.get(), equalTo(new StringNode(TYPE)))
            );
        }

        @DisplayName("[state]")
        @ParameterizedTest(name = "should extract for path: {0}")
        @ValueSource(strings = {
                "[state]",
                "[STATE]", // Case insensitive
        })
        void extractState(String path) {
            final RecordExtractor extractor = new RecordExtractor(record());
            final Optional<JsonNode> value = extractor.extract(path);

            assertAll(
                    () -> assertTrue(value.isPresent(), "Optional#isPresent()"),
                    () -> assertThat(value.get(), equalTo(new StringNode(STATE)))
            );
        }

        @DisplayName("[id]")
        @ParameterizedTest(name = "should extract for path: {0}")
        @ValueSource(strings = {
                "[id]",
                "[ID]", // Case insensitive
                "[reference]", // Alias
                "[REFERENCE]", // Alias, case-insensitive
                "[case_reference]", // Legacy alias
                "[CASE_REFERENCE]", // Legacy alias, case-insensitive
        })
        void extractId(String path) {
            final RecordExtractor extractor = new RecordExtractor(record());
            final Optional<JsonNode> value = extractor.extract(path);

            assertAll(
                    () -> assertTrue(value.isPresent(), "Optional#isPresent()"),
                    () -> assertThat(value.get(), equalTo(new StringNode(REFERENCE)))
            );
        }

        @DisplayName("[classification]")
        @ParameterizedTest(name = "should extract for path: {0}")
        @ValueSource(strings = {
                "[classification]",
                "[CLASSIFICATION]", // Case insensitive
                "[security_classification]", // Alias
                "[SECURITY_CLASSIFICATION]", // Alias, case-insensitive
        })
        void extractClassification(String path) {
            final RecordExtractor extractor = new RecordExtractor(record());
            final Optional<JsonNode> value = extractor.extract(path);

            assertAll(
                    () -> assertTrue(value.isPresent(), "Optional#isPresent()"),
                    () -> assertThat(value.get(), equalTo(new StringNode(CLASSIFICATION)))
            );
        }

        @DisplayName("[created]")
        @ParameterizedTest(name = "should extract for path: {0}")
        @ValueSource(strings = {
                "[created]",
                "[CREATED]", // Case insensitive
                "[created_date]", // Legacy alias
                "[CREATED_DATE]", // Legacy alias, case-insensitive
        })
        void extractCreatedDate(String path) {
            final RecordExtractor extractor = new RecordExtractor(record());
            final Optional<JsonNode> value = extractor.extract(path);

            assertAll(
                    () -> assertTrue(value.isPresent(), "Optional#isPresent()"),
                    () -> assertThat(value.get(), equalTo(new StringNode(CREATED.toString())))
            );
        }

        @DisplayName("[modified]")
        @ParameterizedTest(name = "should extract for path: {0}")
        @ValueSource(strings = {
                "[modified]",
                "[MODIFIED]", // Case insensitive
                "[last_modified]", // Alias
                "[LAST_MODIFIED]", // Alias, case-insensitive
        })
        void extractLastModifiedDate(String path) {
            final RecordExtractor extractor = new RecordExtractor(record());
            final Optional<JsonNode> value = extractor.extract(path);

            assertAll(
                    () -> assertTrue(value.isPresent(), "Optional#isPresent()"),
                    () -> assertThat(value.get(), equalTo(new StringNode(MODIFIED.toString())))
            );
        }
    }

    @Nested
    @DisplayName("when nested in complex field")
    class ComplexField {
        @Test
        @DisplayName("should extract nested field from record data")
        void nestedField() {
            final RecordExtractor extractor = new RecordExtractor(record());
            final Optional<JsonNode> nestedValue = extractor.extract("complexField.level1.level2");

            assertAll(
                    () -> assertTrue(nestedValue.isPresent(), "Optional#isPresent()"),
                    () -> assertThat(nestedValue.get(), equalTo(new StringNode("nested value 2")))
            );
        }

        @Test
        @DisplayName("should return empty optional when parent not found")
        void parentNotFound() {
            final RecordExtractor extractor = new RecordExtractor(record());
            final Optional<JsonNode> nestedValue = extractor.extract("notAField.level1.level2");

            assertTrue(nestedValue.isEmpty(), "Optional#isEmpty()");
        }

        @Test
        @DisplayName("should return empty optional when child not found")
        void childNotFound() {
            final RecordExtractor extractor = new RecordExtractor(record());
            final Optional<JsonNode> nestedValue = extractor.extract("complexField.level1.notAField");

            assertTrue(nestedValue.isEmpty(), "Optional#isEmpty()");
        }

        @Test
        @DisplayName("should return empty optional when parent is not an object")
        void parentNotObject() {
            final RecordExtractor extractor = new RecordExtractor(record());
            final Optional<JsonNode> nestedValue = extractor.extract("textField.level1.level2");

            assertTrue(nestedValue.isEmpty(), "Optional#isEmpty()");
        }
    }

    @Nested
    @DisplayName("when nested in collection field")
    class CollectionField {
        @Test
        @DisplayName("should extract simple collection item from record data using item index")
        void simpleCollectionItemByIndex() {
            final RecordExtractor extractor = new RecordExtractor(record());
            final Optional<JsonNode> nestedValue = extractor.extract("complexField.nestedSimpleCollection[1].value");

            assertAll(
                    () -> assertTrue(nestedValue.isPresent(), "Optional#isPresent()"),
                    () -> assertThat(nestedValue.get(), equalTo(new StringNode("Collection value 2")))
            );
        }

        @Test
        @DisplayName("should return empty optional when no item found for index")
        void itemNotFoundByIndex() {
            final RecordExtractor extractor = new RecordExtractor(record());
            final Optional<JsonNode> nestedValue = extractor.extract("complexField.nestedSimpleCollection[42].value");

            assertTrue(nestedValue.isEmpty(), "Optional#isEmpty()");
        }

        @Test
        @DisplayName("should extract simple collection item from record data using item ID")
        void simpleCollectionItemById() {
            final RecordExtractor extractor = new RecordExtractor(record());
            final Optional<JsonNode> nestedValue = extractor.extract("complexField.nestedSimpleCollection[id:789].value");

            assertAll(
                    () -> assertTrue(nestedValue.isPresent(), "Optional#isPresent()"),
                    () -> assertThat(nestedValue.get(), equalTo(new StringNode("Collection value 3")))
            );
        }

        @Test
        @DisplayName("should return empty optional when no item found for ID")
        void itemNotFoundById() {
            final RecordExtractor extractor = new RecordExtractor(record());
            final Optional<JsonNode> nestedValue = extractor.extract("complexField.nestedSimpleCollection[id:not-found].value");

            assertTrue(nestedValue.isEmpty(), "Optional#isEmpty()");
        }

        @Test
        @DisplayName("should return empty optional when field is not a collection")
        void notACollection() {
            final RecordExtractor extractor = new RecordExtractor(record());
            final Optional<JsonNode> nestedValue = extractor.extract("complexField.level1[id:123].value");

            assertTrue(nestedValue.isEmpty(), "Optional#isEmpty()");
        }

        @Test
        @DisplayName("should throw error when index is malformed")
        void throwWhenIndexMalformed() {
            final RecordExtractor extractor = new RecordExtractor(record());

            final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                                                    () -> extractor.extract("complexField.nestedSimpleCollection[a].value"));
            assertThat(exception.getMessage(), equalTo("Empty or invalid path element: nestedSimpleCollection[a]"));
        }
    }

    private Record record() {
        return new Record() {
            @Override
            public String getWorkspace() {
                return WORKSPACE;
            }

            @Override
            public String getType() {
                return TYPE;
            }

            @Override
            public String getReference() {
                return REFERENCE;
            }

            @Override
            public String getState() {
                return STATE;
            }

            @Override
            public LocalDateTime getCreated() {
                return CREATED;
            }

            @Override
            public LocalDateTime getLastModified() {
                return MODIFIED;
            }

            @Override
            public String getClassification() {
                return CLASSIFICATION;
            }

            @Override
            public ObjectNode getData() {
                try {
                    return (ObjectNode) mapper.readTree(DATA);
                } catch (JacksonException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public ObjectNode getDataClassification() {
                return null;
            }
        };
    }
}
