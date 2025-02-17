package app.quickcase.sdk.spring.definition;

import java.util.Map;

import app.quickcase.sdk.spring.definition.model.DataField;
import app.quickcase.sdk.spring.definition.model.MetadataField;
import app.quickcase.sdk.spring.definition.model.RecordType;
import app.quickcase.sdk.spring.definition.model.Schema;
import app.quickcase.sdk.spring.path.FieldPath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefinitionExtractorTest {

    @Nested
    class ExtractMetadataField {
        @Test
        @DisplayName("should return workspace field")
        void shouldReturnWorkspaceField() {
            var extractor = new DefinitionExtractor(recordType());
            var field = extractor.extractField(FieldPath.ofMetadata("[workspace]"));

            assertThat(field, equalTo(
                    MetadataField.builder()
                                 .id("[workspace]")
                                 .name("Workspace")
                                 .label("Workspace")
                                 .build()
            ));
        }

        @Test
        @DisplayName("should return type field")
        void shouldReturnTypeField() {
            var extractor = new DefinitionExtractor(recordType());
            var field = extractor.extractField(FieldPath.ofMetadata("[type]"));

            assertThat(field, equalTo(
                    MetadataField.builder()
                                 .id("[type]")
                                 .name("Type")
                                 .label("Record type")
                                 .build()
            ));
        }

        @Test
        @DisplayName("should return ID field")
        void shouldReturnIdField() {
            var extractor = new DefinitionExtractor(recordType());
            var field = extractor.extractField(FieldPath.ofMetadata("[id]"));

            assertThat(field, equalTo(
                    MetadataField.builder()
                                 .id("[id]")
                                 .name("ID")
                                 .label("Record ID")
                                 .build()
            ));
        }

        @Test
        @DisplayName("should return title field")
        void shouldReturnTitleField() {
            var extractor = new DefinitionExtractor(recordType());
            var field = extractor.extractField(FieldPath.ofMetadata("[title]"));

            assertThat(field, equalTo(
                    MetadataField.builder()
                                 .id("[title]")
                                 .name("Title")
                                 .label("Record title")
                                 .build()
            ));
        }

        @Test
        @DisplayName("should return state field")
        void shouldReturnStateField() {
            var extractor = new DefinitionExtractor(recordType());
            var field = extractor.extractField(FieldPath.ofMetadata("[state]"));

            assertThat(field, equalTo(
                    MetadataField.builder()
                                 .id("[state]")
                                 .name("State")
                                 .label("Record state")
                                 .build()
            ));
        }

        @Test
        @DisplayName("should return classification field")
        void shouldReturnClassificationField() {
            var extractor = new DefinitionExtractor(recordType());
            var field = extractor.extractField(FieldPath.ofMetadata("[classification]"));

            assertThat(field, equalTo(
                    MetadataField.builder()
                                 .id("[classification]")
                                 .name("Classification")
                                 .label("Default record classification")
                                 .build()
            ));
        }

        @Test
        @DisplayName("should return createdAt field")
        void shouldReturnCreatedAtField() {
            var extractor = new DefinitionExtractor(recordType());
            var field = extractor.extractField(FieldPath.ofMetadata("[createdAt]"));

            assertThat(field, equalTo(
                    MetadataField.builder()
                                 .id("[createdAt]")
                                 .name("Created at")
                                 .label("Date and time of creation")
                                 .build()
            ));
        }

        @Test
        @DisplayName("should return lastModifiedAt field")
        void shouldReturnLastModifiedAtField() {
            var extractor = new DefinitionExtractor(recordType());
            var field = extractor.extractField(FieldPath.ofMetadata("[lastModifiedAt]"));

            assertThat(field, equalTo(
                    MetadataField.builder()
                                 .id("[lastModifiedAt]")
                                 .name("Last modified at")
                                 .label("Date and time of last modification")
                                 .build()
            ));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "[sourceFieldPath]"
        })
        @DisplayName("should throw error for paths not supported in definition context")
        void shouldThrowErrorForPathsNotSupported(String path) {
            var extractor = new DefinitionExtractor(recordType());
            var error = assertThrows(
                    IllegalArgumentException.class,
                    () -> extractor.extractField(FieldPath.ofMetadata(path))
            );

            assertThat(error.getMessage(), equalTo("Path not supported in definition context: " + path));
        }
    }

    @Nested
    class ExtractDataField {
        @Test
        @DisplayName("should return empty when field does not exist in definition")
        void shouldReturnEmptyWhenFieldDoesNotExist() {
            var extractor = new DefinitionExtractor(recordType());

            var field = extractor.extractField(FieldPath.ofData("notAField"));

            assertThat(field.isEmpty(), is(true));
        }

        @Test
        @DisplayName("should return definition of top level field found")
        void shouldReturnTopLevelField() {
            var type = recordType();
            var extractor = new DefinitionExtractor(type);

            var field = extractor.extractField(FieldPath.ofData("field1"));

            assertThat(field.orElseThrow(), equalTo(
                    type.schema().fields().get("field1")
            ));
        }

        @Test
        @DisplayName("should return definition of nested complex member field found")
        void shouldReturnNestedComplexMemberField() {
            var type = recordType();
            var extractor = new DefinitionExtractor(type);

            var field = extractor.extractField(FieldPath.ofData("complex1.member1.member11"));

            assertThat(field.orElseThrow(), equalTo(
                    type.schema().fields()
                        .get("complex1")
                        .members().get("member1")
                        .members().get("member11")
            ));
        }

        @Test
        @DisplayName("should return definition of collection content found")
        void shouldReturnCollectionContent() {
            var type = recordType();
            var extractor = new DefinitionExtractor(type);

            var field = extractor.extractField(FieldPath.ofData("collection1[].value"));

            var collectionField = type.schema().fields().get("collection1");
            assertThat(field.orElseThrow(), equalTo(
                    DataField.builder()
                             .id("collection1[].value")
                             .name(collectionField.name())
                             .type(collectionField.content().type())
                             .members(
                                     collectionField.content().members()
                             )
                             .acl(collectionField.acl())
                             .classification(collectionField.classification())
                             .build()
            ));
        }

        @Test
        @DisplayName("should return definition of complex member found in collection content")
        void shouldReturnComplexMemberInCollection() {
            var type = recordType();
            var extractor = new DefinitionExtractor(type);

            var field = extractor.extractField(FieldPath.ofData("collection1[].value.member1"));

            assertThat(field.orElseThrow(), equalTo(
                    type.schema().fields().get("collection1").content().members().get("member1")
            ));
        }
    }

    private RecordType recordType() {
        var fields = Map.of(
                "field1",
                DataField.builder()
                         .id("field1")
                         .name("Field 1")
                         .type("text")
                         .acl(Map.of())
                         .classification("PUBLIC")
                         .build(),
                "complex1",
                DataField.builder()
                         .id("complex1")
                         .name("Complex 1")
                         .type("complex")
                         .member(
                                 "member1",
                                 DataField.builder()
                                          .id("member1")
                                          .name("Member 1")
                                          .type("complex")
                                          .member(
                                                  "member11",
                                                  DataField.builder()
                                                           .id("member11")
                                                           .name("Member 11")
                                                           .type("text")
                                                           .acl(Map.of())
                                                           .classification("PUBLIC")
                                                           .build()
                                          )
                                          .acl(Map.of())
                                          .classification("PUBLIC")
                                          .build()
                         )
                         .acl(Map.of())
                         .classification("PUBLIC")
                         .build(),
                "collection1",
                DataField.builder()
                         .id("collection1")
                         .name("Collection 1")
                         .type("collection")
                         .content(
                                 DataField.Content.builder()
                                                  .type("complex")
                                                  .member(
                                                          "member1",
                                                          DataField.builder()
                                                                   .id("member1")
                                                                   .name("Member 1")
                                                                   .type("text")
                                                                   .acl(Map.of())
                                                                   .classification("PUBLIC")
                                                                   .build()
                                                  )
                                                  .build()
                         )
                         .acl(Map.of())
                         .classification("PUBLIC")
                         .build()

        );

        return RecordType
                .builder()
                .schema(Schema.builder().fields(fields).build())
                .build();
    }

}