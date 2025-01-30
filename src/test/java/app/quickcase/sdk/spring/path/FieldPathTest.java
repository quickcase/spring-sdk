package app.quickcase.sdk.spring.path;

import app.quickcase.sdk.spring.metadata.Metadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FieldPathTest {

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