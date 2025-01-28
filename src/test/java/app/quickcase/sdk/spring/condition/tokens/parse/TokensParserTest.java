package app.quickcase.sdk.spring.condition.tokens.parse;

import java.util.List;

import app.quickcase.sdk.spring.condition.ConditionNode;
import app.quickcase.sdk.spring.condition.Criteria;
import app.quickcase.sdk.spring.condition.Group;
import app.quickcase.sdk.spring.condition.tokens.parse.TokensParser;
import app.quickcase.sdk.spring.condition.tokens.parse.error.SyntaxException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static app.quickcase.sdk.spring.condition.BinaryOperator.AND;
import static app.quickcase.sdk.spring.condition.BinaryOperator.OR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class TokensParserTest {

    private static String[] condition(String...tokens) {
        return tokens;
    }

    private static Criteria.CriteriaBuilder criteria(String path, String operator, Object value) {
        return Criteria.builder().path(path).operator(operator).value(value);
    }

    private static Group group(ConditionNode...members) {
        return new Group(false, List.of(members));
    }

    private static Group negatedGroup(ConditionNode...members) {
        return new Group(true, List.of(members));
    }

    @Test
    @DisplayName("should parse simple conjunction condition without grouping")
    void shouldParseSimpleConjunctionNoGrouping() {
        final String[] tokens = condition(
            "field1", "===", "\"value1\"",
            "AND",
            "field2", "===", "\"Yes\""
        );

        final TokensParser parser = new TokensParser();
        assertThat(parser.parse(tokens), equalTo(new ConditionNode[]{
            criteria("field1", "EQUALS", "value1").build(),
            AND,
            criteria("field2", "EQUALS", "Yes").build(),
        }));
    }

    @Test
    @DisplayName("should parse simple condition with redundant grouping")
    void shouldParseSimpleConditionWithRedundantGrouping() {
        final String[] tokens = condition(
            "(", "field1", "===", "\"value1\"", ")"
        );

        final TokensParser parser = new TokensParser();
        assertThat(parser.parse(tokens), equalTo(new ConditionNode[]{
            group(
                criteria("field1", "EQUALS", "value1").build()
            )
        }));
    }

    @Test
    @DisplayName("should parse composed condition with one level of grouping")
    void shouldParseComposedConditionWithSingleLevelGrouping() {
        final String[] tokens = condition(
            "(", "a", "===", "\"1\"", "AND", "b", "===", "\"2\"", ")",
            "OR",
            "(", "c", "===", "\"3\"", "AND", "d", "===", "\"4\"", ")"
        );

        final TokensParser parser = new TokensParser();
        assertThat(parser.parse(tokens), equalTo(new ConditionNode[]{
            group(
                criteria("a", "EQUALS", "1").build(),
                AND,
                criteria("b", "EQUALS", "2").build()
            ),
            OR,
            group(
                criteria("c", "EQUALS", "3").build(),
                AND,
                criteria("d", "EQUALS", "4").build()
            ),
        }));
    }

    @Test
    @DisplayName("should parse conditions with nested groups")
    void shouldParseConditionWithNestedGrouping() {
        final String[] tokens = condition(
            "(",
                "(", "a", "===", "\"1\"", ")",
                "AND",
                "(", "b", "===", "\"2\"", ")",
            ")",
            "OR",
            "c", "===", "\"3\""
        );

        final TokensParser parser = new TokensParser();
        assertThat(parser.parse(tokens), equalTo(new ConditionNode[]{
            group(
                group(
                    criteria("a", "EQUALS", "1").build()
                ),
                AND,
                group(
                    criteria("b", "EQUALS", "2").build()
                )
            ),
            OR,
            criteria("c", "EQUALS", "3").build(),
        }));
    }

    @Test
    @DisplayName("should reject GROUP_END outside of group")
    void shouldRejectGroupEndOutsideOfGroup() {
        final String[] tokens = condition("field1", "===", "\"value1\"", ")");

        final TokensParser parser = new TokensParser();
        final SyntaxException exception = Assertions.assertThrows(SyntaxException.class,
                                                                  () -> parser.parse(tokens));
        assertThat(
            exception.getMessage(),
            equalTo("Unexpected token ')', expected one of: BINARY_OPERATOR, END")
        );
    }

    @Test
    @DisplayName("should reject condition ending with non-terminal token: Missing value")
    void shouldRejectConditionMissingValue() {
        final String[] tokens = condition("field1", "===");

        final TokensParser parser = new TokensParser();
        final SyntaxException exception = Assertions.assertThrows(SyntaxException.class,
                                                                  () -> parser.parse(tokens));
        assertThat(
            exception.getMessage(),
            equalTo("Unexpected end of condition, expected one of: VALUE_NUMBER, VALUE_QUOTED")
        );
    }

    @Test
    @DisplayName("should reject condition ending with non-terminal token: Group not closed")
    void shouldRejectConditionGroupNotClosed() {
        final String[] tokens = condition("(", "field1", "===", "\"value1\"");

        final TokensParser parser = new TokensParser();
        final SyntaxException exception = Assertions.assertThrows(SyntaxException.class,
                                                                  () -> parser.parse(tokens));
        assertThat(
            exception.getMessage(),
            equalTo("Unexpected end of condition, expected one of: BINARY_OPERATOR, GROUP_END")
        );
    }

    @Test
    @DisplayName("should parse negated condition")
    void shouldParseNegatedCondition() {
        final String[] tokens = condition(
            "(", "a", "===", "\"1\"", "AND", "NOT", "b", "===", "\"2\"", ")",
            "OR",
            "NOT", "(", "c", "===", "\"3\"", "AND", "NOT", "(", "d", "===", "\"4\"", "OR", "e", "===", "\"5\"", ")", ")"
        );

        final TokensParser parser = new TokensParser();
        assertThat(parser.parse(tokens), equalTo(new ConditionNode[]{
            group(
                criteria("a", "EQUALS", "1").build(),
                AND,
                criteria("b", "EQUALS", "2").negated(true).build()
            ),
            OR,
            negatedGroup(
                criteria("c", "EQUALS", "3").negated(true).build(),
                OR,
                group(
                    criteria("d", "EQUALS", "4").build(),
                    OR,
                    criteria("e", "EQUALS", "5").build()
                )
            )
        }));
    }

    @Nested
    @DisplayName("EQUALS")
    class EqualsOperator {
        @Test
        @DisplayName("should parse all forms of EQUALS operator")
        void shouldParseAllEqualsOperator() {
            final String[] tokens = condition(
                // Case insensitive
                "field1", "=", "\"a\"", "AND",
                "field2", "==", "\"b\"", "AND",
                "field3", "EQUALS_IC", "\"c\"", "AND",
                // Case sensitive
                "field4", "===", "\"d\"", "AND",
                "field5", "EQUALS", "\"e\""
            );

            final TokensParser parser = new TokensParser();
            assertThat(parser.parse(tokens), equalTo(new ConditionNode[]{
                criteria("field1", "EQUALS", "a").ignoreCase(true).build(),
                AND,
                criteria("field2", "EQUALS", "b").ignoreCase(true).build(),
                AND,
                criteria("field3", "EQUALS", "c").ignoreCase(true).build(),
                AND,
                criteria("field4", "EQUALS", "d").build(),
                AND,
                criteria("field5", "EQUALS", "e").build(),
            }));
        }

        @Test
        @DisplayName("should accept numeric criteria value")
        void shouldAcceptNumericCriteriaValue() {
            final String[] tokens = condition("field1", "EQUALS", "1");

            final TokensParser parser = new TokensParser();
            assertThat(parser.parse(tokens), equalTo(new ConditionNode[]{
                criteria("field1", "EQUALS", 1).build(),
            }));
        }

        @Test
        @DisplayName("should reject other values")
        void shouldRejectOtherValues() {
            final String[] tokens = condition(
                "field1", "=", "abc" // Non-quoted string and non-numeric
            );

            final TokensParser parser = new TokensParser();
            final SyntaxException exception = Assertions.assertThrows(SyntaxException.class,
                                                                      () -> parser.parse(tokens));
            assertThat(
                exception.getMessage(),
                equalTo("Unexpected token 'abc', expected one of: VALUE_NUMBER, VALUE_QUOTED")
            );
        }
    }

    @Nested
    @DisplayName("STARTS_WITH")
    class StartsWithOperator {
        @Test
        @DisplayName("should parse all forms of STARTS_WITH operator")
        void shouldParseAllStartsWithOperator() {
            final String[] tokens = condition(
                // Case insensitive
                "field1", "STARTS_WITH_IC", "\"a\"", "AND",
                // Case sensitive
                "field2", "STARTS_WITH", "\"b\""
            );

            final TokensParser parser = new TokensParser();
            assertThat(parser.parse(tokens), equalTo(new ConditionNode[]{
                criteria("field1", "STARTS_WITH", "a").ignoreCase(true).build(),
                AND,
                criteria("field2", "STARTS_WITH", "b").build(),
            }));
        }

        @Test
        @DisplayName("should reject numeric values")
        void shouldRejectNumericValues() {
            final String[] tokens = condition(
                "field1", "STARTS_WITH", "123"
            );

            final TokensParser parser = new TokensParser();
            final SyntaxException exception = Assertions.assertThrows(SyntaxException.class,
                                                                      () -> parser.parse(tokens));
            assertThat(
                exception.getMessage(),
                equalTo("Unexpected token '123', expected one of: VALUE_QUOTED")
            );
        }
    }

    @Nested
    @DisplayName("ENDS_WITH")
    class EndsWithOperator {
        @Test
        @DisplayName("should parse all forms of ENDS_WITH operator")
        void shouldParseAllEndsWithOperator() {
            final String[] tokens = condition(
                // Case insensitive
                "field1", "ENDS_WITH_IC", "\"a\"", "AND",
                // Case sensitive
                "field2", "ENDS_WITH", "\"b\""
            );

            final TokensParser parser = new TokensParser();
            assertThat(parser.parse(tokens), equalTo(new ConditionNode[]{
                criteria("field1", "ENDS_WITH", "a").ignoreCase(true).build(),
                AND,
                criteria("field2", "ENDS_WITH", "b").build(),
            }));
        }

        @Test
        @DisplayName("should reject numeric values")
        void shouldRejectNumericValues() {
            final String[] tokens = condition(
                "field1", "ENDS_WITH", "123"
            );

            final TokensParser parser = new TokensParser();
            final SyntaxException exception = Assertions.assertThrows(SyntaxException.class,
                                                                      () -> parser.parse(tokens));
            assertThat(
                exception.getMessage(),
                equalTo("Unexpected token '123', expected one of: VALUE_QUOTED")
            );
        }
    }

    @Nested
    @DisplayName("CONTAINS")
    class ContainsOperator {
        @Test
        @DisplayName("should parse all forms of CONTAINS operator")
        void shouldParseAllContainsOperator() {
            final String[] tokens = condition(
                // Case insensitive
                "field1", "CONTAINS_IC", "\"a\"", "AND",
                // Case sensitive
                "field2", "CONTAINS", "\"b\""
            );

            final TokensParser parser = new TokensParser();
            assertThat(parser.parse(tokens), equalTo(new ConditionNode[]{
                criteria("field1", "CONTAINS", "a").ignoreCase(true).build(),
                AND,
                criteria("field2", "CONTAINS", "b").build(),
            }));
        }

        @Test
        @DisplayName("should accept numeric criteria value")
        void shouldAcceptNumericCriteriaValue() {
            final String[] tokens = condition("field1", "CONTAINS", "1");

            final TokensParser parser = new TokensParser();
            assertThat(parser.parse(tokens), equalTo(new ConditionNode[]{
                criteria("field1", "CONTAINS", 1).build(),
            }));
        }
    }

    @Nested
    @DisplayName("MATCHES")
    class MatchesOperator {
        @Test
        @DisplayName("should parse MATCHES operator")
        void shouldParseMatchesOperator() {
            final String[] tokens = condition(
                "field1", "MATCHES", "\"^[a-z]{3}$\""
            );

            final TokensParser parser = new TokensParser();
            assertThat(parser.parse(tokens), equalTo(new ConditionNode[]{
                criteria("field1", "MATCHES", "^[a-z]{3}$").build(),
            }));
        }

        @Test
        @DisplayName("should reject numeric values")
        void shouldRejectNumericValues() {
            final String[] tokens = condition(
                "field1", "MATCHES", "123"
            );

            final TokensParser parser = new TokensParser();
            final SyntaxException exception = Assertions.assertThrows(SyntaxException.class,
                                                                      () -> parser.parse(tokens));
            assertThat(
                exception.getMessage(),
                equalTo("Unexpected token '123', expected one of: VALUE_QUOTED")
            );
        }
    }

    @Nested
    @DisplayName("HAS_LENGTH")
    class HasLengthOperator {
        @Test
        @DisplayName("should parse HAS_LENGTH operator")
        void shouldParseAllContainsOperator() {
            final String[] tokens = condition("field1", "HAS_LENGTH", "3");

            final TokensParser parser = new TokensParser();
            assertThat(parser.parse(tokens), equalTo(new ConditionNode[]{
                criteria("field1", "HAS_LENGTH", 3).build(),
            }));
        }

        @Test
        @DisplayName("should reject quoted string values")
        void shouldRejectQuotedStringValues() {
            final String[] tokens = condition("field1", "HAS_LENGTH", "\"abc\"");

            final TokensParser parser = new TokensParser();
            final SyntaxException exception = Assertions.assertThrows(SyntaxException.class,
                                                                      () -> parser.parse(tokens));
            assertThat(
                exception.getMessage(),
                equalTo("Unexpected token '\"abc\"', expected one of: VALUE_NUMBER")
            );
        }
    }
}
