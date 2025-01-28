package app.quickcase.sdk.spring.condition;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class ConditionParserTest {

    private static Condition condition(Criteria[]... disjunctions) {
        return new Condition(disjunctions);
    }

    private static Criteria[] conjunctions(Criteria... conjunctions) {
        return conjunctions;
    }

    private static Criteria.CriteriaBuilder criteria(String path, String operator, Object value) {
        return Criteria.builder().path(path).operator(operator).value(value);
    }

    @Test
    @DisplayName("Simple condition")
    void simpleCondition() {
        assertCondition(
            "field1 ==\"value 1\"",
            condition(
                conjunctions(
                    criteria("field1", "EQUALS", "value 1").ignoreCase(true).build()
                )
            )
        );
    }

    @Test
    @DisplayName("Simple negated condition")
    void simpleNegatedCondition() {
        assertCondition(
            "NOT field1 ==\"value 1\"",
            condition(
                conjunctions(
                    criteria("field1", "EQUALS", "value 1").ignoreCase(true).negated(true).build()
                )
            )
        );
    }

    @Test
    @DisplayName("Simple condition with conjunction")
    void simpleConditionWithConjunction() {
        assertCondition(
            "field1 == \"value 1\" AND NOT field2 === \"value 2\"",
            condition(
                conjunctions(
                    criteria("field1", "EQUALS", "value 1").ignoreCase(true).build(),
                    criteria("field2", "EQUALS", "value 2").negated(true).build()
                )
            )
        );
    }

    @Test
    @DisplayName("Simple condition with disjunction")
    void simpleConditionWithDisjunction() {
        assertCondition(
            "field1 MATCHES \"^[abc]+\" OR NOT(field2 STARTS_WITH_IC \"value 2\")",
            condition(
                conjunctions(
                    criteria("field1", "MATCHES", "^[abc]+").build()
                ),
                conjunctions(
                    criteria("field2", "STARTS_WITH", "value 2").ignoreCase(true).negated(true).build()
                )
            )
        );
    }

    @Test
    @DisplayName("Condition with 1-level grouping")
    void singleLevelGrouping() {
        assertCondition(
            "(field1 EQUALS \"value 1\" AND field2 EQUALS_IC \"value 2\") " +
                "OR (field1 EQUALS \"value A\" AND field2 EQUALS_IC \"value B\")",
            condition(
                conjunctions(
                    criteria("field1", "EQUALS", "value 1").build(),
                    criteria("field2", "EQUALS", "value 2").ignoreCase(true).build()
                ),
                conjunctions(
                    criteria("field1", "EQUALS", "value A").build(),
                    criteria("field2", "EQUALS", "value B").ignoreCase(true).build()
                )
            )
        );
    }

    @Test
    @DisplayName("Condition with negated 1-level grouping")
    void singleLevelNegatedGrouping() {
        assertCondition(
            "field1 EQUALS \"value 1\" AND NOT (field2 EQUALS \"value B\" AND field3 EQUALS \"value C\")",
            condition(
                conjunctions(
                    criteria("field1", "EQUALS", "value 1").build(),
                    criteria("field2", "EQUALS", "value B").negated(true).build()
                ),
                conjunctions(
                    criteria("field1", "EQUALS", "value 1").build(),
                    criteria("field3", "EQUALS", "value C").negated(true).build()
                )
            )
        );
    }

    @Test
    @DisplayName("Condition with N-level grouping")
    void nLevelGrouping() {
        assertCondition(
            "\n" +
                "    (\n" +
                "      (\n" +
                "        (complex.field1 EQUALS \"value 1\")\n" +
                "        AND\n" +
                "        (\n" +
                "          complex.field2 EQUALS \"value 2\" OR complex.field3 EQUALS \"value 3\"\n" +
                "        )\n" +
                "      )\n" +
                "      OR\n" +
                "      complex.field4 HAS_LENGTH 4\n" +
                "    )\n" +
                "    OR\n" +
                "    complex.field5 EQUALS \"value 5\"\n" +
                "",
            condition(
                conjunctions(
                    criteria("complex.field1", "EQUALS", "value 1").build(),
                    criteria("complex.field2", "EQUALS", "value 2").build()
                ),
                conjunctions(
                    criteria("complex.field1", "EQUALS", "value 1").build(),
                    criteria("complex.field3", "EQUALS", "value 3").build()
                ),
                conjunctions(
                    criteria("complex.field4", "HAS_LENGTH", 4).build()
                ),
                conjunctions(
                    criteria("complex.field5", "EQUALS", "value 5").build()
                )
            )
        );
        
    }

    private void assertCondition(String conditionString, Condition expected) {
        final ConditionParser parser = new ConditionParser();
        assertThat(parser.parse(conditionString), equalTo(expected));
    }
}
