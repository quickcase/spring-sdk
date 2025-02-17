package app.quickcase.sdk.spring.condition.validate;

import java.util.Map;
import java.util.Set;

import app.quickcase.sdk.spring.condition.Condition;
import app.quickcase.sdk.spring.condition.Criteria;
import app.quickcase.sdk.spring.definition.DefinitionExtractor;
import app.quickcase.sdk.spring.definition.model.DataField;
import app.quickcase.sdk.spring.definition.model.RecordType;
import app.quickcase.sdk.spring.definition.model.Schema;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class SemanticConditionValidatorTest {

    @Test
    @DisplayName("should return empty set when all criteria path are valid")
    void shouldReturnEmptySetWhenAllValid() {
        var fieldExtractor = new DefinitionExtractor(recordType());
        var validator = new SemanticConditionValidator(fieldExtractor);

        var condition = new Condition(
                new Criteria[][]{
                        new Criteria[]{
                                Criteria.builder().path("complex1").build(),
                                Criteria.builder().path("[sourceFieldPath]").build()
                        },
                        new Criteria[]{
                                Criteria.builder().path("complex1.member1").build(),
                                Criteria.builder().path("complex1.member1.member11").build(),
                        },
                }
        );

        var errors = validator.validate(condition);

        assertThat(errors, equalTo(Set.of()));
    }

    @Test
    @DisplayName("should return errors for all invalid criteria paths")
    void shouldReturnErrorsForInvalidCriteriaPaths() {
        var fieldExtractor = new DefinitionExtractor(recordType());
        var validator = new SemanticConditionValidator(fieldExtractor);

        var condition = new Condition(
                new Criteria[][]{
                        new Criteria[]{
                                Criteria.builder().path("invalid syntax").build()
                        },
                        new Criteria[]{
                                Criteria.builder().path("complex1.member1").build(),
                                Criteria.builder().path("fieldNotFound").build(),
                        },
                }
        );

        var errors = validator.validate(condition);

        assertThat(errors, equalTo(Set.of(
                "Path `fieldNotFound` does not exist",
                "Path `invalid syntax` is not valid"
        )));
    }

    private RecordType recordType() {
        var fields = Map.of(
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
                         .build()

        );

        return RecordType
                .builder()
                .schema(Schema.builder().fields(fields).build())
                .build();
    }
}