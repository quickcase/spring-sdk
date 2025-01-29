package app.quickcase.sdk.spring.metadata;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MetadataTest {

    @Nested
    class FromPath {
        @Test
        @DisplayName("should throw error when path not valid")
        void shouldThrowErrorWhenPathTooShort() {
            var error = assertThrows(IllegalArgumentException.class, () -> Metadata.fromPath("["));
            assertThat(error.getMessage(), equalTo("Invalid metadata path: ["));
        }

        @Test
        @DisplayName("should throw error when path not valid")
        void shouldThrowErrorWhenPathNotValid() {
            var error = assertThrows(IllegalArgumentException.class, () -> Metadata.fromPath("[notValid]"));
            assertThat(error.getMessage(), equalTo("Invalid metadata path: [notValid]"));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "[workspace]",
                "[WORKSPACE]", // Case insensitive
                "[organisation]", // Legacy
                "[jurisdiction]", // Legacy
        })
        @DisplayName("should return workspace metadata")
        void shouldReturnWorkspace(String path) {
            assertThat(Metadata.fromPath(path), is(Metadata.WORKSPACE));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "[type]",
                "[TYPE]", // Case insensitive
                "[case_type]", // Legacy
        })
        @DisplayName("should return type metadata")
        void shouldReturnType(String path) {
            assertThat(Metadata.fromPath(path), is(Metadata.TYPE));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "[id]",
                "[ID]", // Case insensitive
                "[reference]", // Legacy
                "[case_reference]", // Legacy
        })
        @DisplayName("should return id metadata")
        void shouldReturnId(String path) {
            assertThat(Metadata.fromPath(path), is(Metadata.ID));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "[title]",
                "[TITLE]", // Case insensitive
        })
        @DisplayName("should return title metadata")
        void shouldReturnTitle(String path) {
            assertThat(Metadata.fromPath(path), is(Metadata.TITLE));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "[state]",
                "[STATE]", // Case insensitive
        })
        @DisplayName("should return state metadata")
        void shouldReturnState(String path) {
            assertThat(Metadata.fromPath(path), is(Metadata.STATE));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "[classification]",
                "[CLASSIFICATION]", // Case insensitive
                "[security_classification]", // Legacy
        })
        @DisplayName("should return classification metadata")
        void shouldReturnClassification(String path) {
            assertThat(Metadata.fromPath(path), is(Metadata.CLASSIFICATION));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "[createdat]",
                "[CreatedAt]", // Case insensitive
                "[created]", // Legacy
                "[created_date]", // Legacy
        })
        @DisplayName("should return createdAt metadata")
        void shouldReturnCreatedAt(String path) {
            assertThat(Metadata.fromPath(path), is(Metadata.CREATED_AT));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "[lastmodifiedat]",
                "[LastModifiedAt]", // Case insensitive
                "[modified]", // Legacy
                "[last_modified]", // Legacy
                "[last_modified_date]", // Legacy
        })
        @DisplayName("should return lastModifiedAt metadata")
        void shouldReturnLastModifiedAt(String path) {
            assertThat(Metadata.fromPath(path), is(Metadata.LAST_MODIFIED_AT));
        }
    }

}