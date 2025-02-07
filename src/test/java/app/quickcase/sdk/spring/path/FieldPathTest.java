package app.quickcase.sdk.spring.path;

import app.quickcase.sdk.spring.metadata.Metadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FieldPathTest {

    @Nested
    class Accepts {
        @ParameterizedTest
        @ValueSource(strings = {
                // metadata
                "[type]",
                "[state]",
                // data field
                "field1",
                "another_field",
                "complex1.member1",
                "collection1[].value.member1",
                "collection1[0].value.member1",
                "collection1[id:item_1].value.member1",
                // computed field
                ":backlinksCount",
                ":another_computed_field",
        })
        @DisplayName("should accept valid field path")
        void shouldAcceptValidFieldPath(String path) {
            assertThat(FieldPath.accepts(path), is(true));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                // metadata
                "[invalidMeta]",
                "[state",
                "state]",
                // data field
                "fie:ld1",
                "another field",
                ".field2",
                "complex1..member1",
                "collection1[abc].value.member1",
                "[0]collection1.value.member1",
                "collection1[id:item_1]value.member1",
                // computed field
                ":",
                ":computed:",
                ":com:puted",
                ":computed[0]",
        })
        @DisplayName("should reject invalid field path")
        void shouldRejectInvalidFieldPath(String path) {
            assertThat(FieldPath.accepts(path), is(false));
        }
    }

    @Nested
    class OfMetadata {
        @Test
        @DisplayName("should throw error when path is not for metadata")
        void shouldThrowErrorWhenNotMetadata() {
            var error = assertThrows(IllegalArgumentException.class, () -> FieldPath.ofMetadata("field1"));
            assertThat(error.getMessage(), equalTo("Invalid metadata path: field1"));
        }

        @Test
        @DisplayName("should return metadata field path")
        void shouldReturnMetadataFieldPath() {
            var path = FieldPath.ofMetadata("[state]");

            assertThat(path.toString(), equalTo("[state]"));
            assertThat(path.getMetadata(), is(Metadata.STATE));
        }
    }

    @Nested
    class OfData {
        @Test
        @DisplayName("should throw error when path is not for data field")
        void shouldThrowErrorWhenNotData() {
            var error = assertThrows(IllegalArgumentException.class, () -> FieldPath.ofData("[state]"));
            assertThat(error.getMessage(), equalTo("Invalid data field path: [state]"));
        }

        @Test
        @DisplayName("should return data field path")
        void shouldReturnDataFieldPath() {
            var path = FieldPath.ofData("field1.child2");

            assertThat(path.toString(), equalTo("field1.child2"));
        }
    }

    @Nested
    class OfComputed {
        @Test
        @DisplayName("should throw error when path is not for computed field")
        void shouldThrowErrorWhenNotMetadata() {
            var error = assertThrows(IllegalArgumentException.class, () -> FieldPath.ofComputed("field1"));
            assertThat(error.getMessage(), equalTo("Invalid computed field path: field1"));
        }

        @Test
        @DisplayName("should return computed field path")
        void shouldReturnComputedFieldPath() {
            var path = FieldPath.ofComputed(":linkedRecordsCount");

            assertThat(path.toString(), equalTo(":linkedRecordsCount"));
            assertThat(path.getIdentifier(), equalTo("linkedRecordsCount"));
        }
    }

    @Nested
    class Of {
        @Test
        @DisplayName("should return metadata field path")
        void shouldReturnMetadataFieldPath() {
            var path = FieldPath.of("[state]");

            assertAll(
                    () -> assertThat(path.toString(), equalTo("[state]")),
                    () -> assertThat(path, is(instanceOf(MetadataFieldPath.class)))
            );
        }

        @Test
        @DisplayName("should return computed field path")
        void shouldReturnComputedFieldPath() {
            var path = FieldPath.of(":linkedRecordsCount");

            assertAll(
                    () -> assertThat(path.toString(), equalTo(":linkedRecordsCount")),
                    () -> assertThat(path, is(instanceOf(ComputedFieldPath.class)))
            );
        }

        @Test
        @DisplayName("should return data field path")
        void shouldReturnDataFieldPath() {
            var path = FieldPath.of("field1.child2");

            assertAll(
                    () -> assertThat(path.toString(), equalTo("field1.child2")),
                    () -> assertThat(path, is(instanceOf(DataFieldPath.class)))
            );
        }
    }

}