package app.quickcase.sdk.spring.condition.tokens.parse;

import java.util.List;

import app.quickcase.sdk.spring.condition.ConditionNode;
import app.quickcase.sdk.spring.condition.Criteria;
import app.quickcase.sdk.spring.condition.Group;
import app.quickcase.sdk.spring.condition.tokens.Token;
import app.quickcase.sdk.spring.condition.tokens.parse.error.SyntaxException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static app.quickcase.sdk.spring.condition.BinaryOperator.AND;
import static app.quickcase.sdk.spring.condition.BinaryOperator.OR;
import static app.quickcase.sdk.spring.condition.tokens.Token.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class TokensParserTest {

    private static Token[] condition(Token...tokens) {
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
        var tokens = condition(
                text("field1"), operator("==="), quotedString("value1"),
                text("AND"),
                text("field2"), operator("==="), quotedString("Yes")
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
        var tokens = condition(
                groupDelimiter("("),
                text("field1"), operator("==="), quotedString("value1"),
                groupDelimiter(")")
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
        var tokens = condition(
                groupDelimiter("("),
                text("a"), operator("==="), quotedString("1"), text("AND"), text("b"), operator("==="), quotedString("2"),
                groupDelimiter(")"),
                text("OR"),
                groupDelimiter("("),
                text("c"), operator("==="), quotedString("3"), text("AND"), text("d"), operator("==="), quotedString("4"),
                groupDelimiter(")")
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
        var tokens = condition(
                groupDelimiter("("),
                groupDelimiter("("), text("a"), operator("==="), quotedString("1"), groupDelimiter(")"),
                text("AND"),
                groupDelimiter("("), text("b"), operator("==="), quotedString("2"), groupDelimiter(")"),
                groupDelimiter(")"),
                text("OR"),
                text("c"), operator("==="), quotedString("3")
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
        var tokens = condition(text("field1"), operator("==="), quotedString("value1"), groupDelimiter(")"));

        final TokensParser parser = new TokensParser();
        final SyntaxException exception = Assertions.assertThrows(SyntaxException.class,
                                                                  () -> parser.parse(tokens));
        assertThat(
            exception.getMessage(),
            equalTo("Unexpected token GroupDelimiterToken[value=)], expected one of: BINARY_OPERATOR, END")
        );
    }

    @Test
    @DisplayName("should reject condition ending with non-terminal token: Missing value")
    void shouldRejectConditionMissingValue() {
        var tokens = condition(text("field1"), operator("==="));

        final TokensParser parser = new TokensParser();
        final SyntaxException exception = Assertions.assertThrows(SyntaxException.class,
                                                                  () -> parser.parse(tokens));
        assertThat(
            exception.getMessage(),
            equalTo("Unexpected end of condition, expected one of: VALUE_NUMBER, VALUE_STRING")
        );
    }

    @Test
    @DisplayName("should reject condition ending with non-terminal token: Group not closed")
    void shouldRejectConditionGroupNotClosed() {
        var tokens = condition(groupDelimiter("("), text("field1"), operator("==="), quotedString("value1"));

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
        var tokens = condition(
                groupDelimiter("("),
                text("a"), operator("==="), quotedString("1"), text("AND"), text("NOT"), text("b"), operator("==="), quotedString("2"),
                groupDelimiter(")"),
                text("OR"),
                text("NOT"), groupDelimiter("("),
                text("c"), operator("==="), quotedString("3"), text("AND"), text("NOT"), groupDelimiter("("), text("d"), operator("==="), quotedString("4"), text("OR"), text("e"), operator("==="), quotedString("5"), groupDelimiter(")"),
                groupDelimiter(")")
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

    @Test
    @DisplayName("should parse all valid field path syntaxes")
    void shouldParseAllValidFieldPathSyntaxes() {
        var tokens = condition(
                text("[state]"), operator("==="), quotedString("active"),
                text("AND"), text(":computedField"), operator("==="), number("2"),
                text("AND"), text("complexField.member1"), operator("==="), quotedString("test"),
                text("AND"), text("collectionField[0].value"), operator("==="), quotedString("itemValue"),
                text("AND"), text("collectionField[id:item1]"), operator("==="), quotedString("itemValue")
        );

        final TokensParser parser = new TokensParser();
        assertThat(parser.parse(tokens), equalTo(new ConditionNode[]{
                criteria("[state]", "EQUALS", "active").build(),
                AND,
                criteria(":computedField", "EQUALS", 2).build(),
                AND,
                criteria("complexField.member1", "EQUALS", "test").build(),
                AND,
                criteria("collectionField[0].value", "EQUALS", "itemValue").build(),
                AND,
                criteria("collectionField[id:item1]", "EQUALS", "itemValue").build(),
        }));
    }

    @Nested
    @DisplayName("EQUALS")
    class EqualsOperator {
        @Test
        @DisplayName("should parse all forms of EQUALS operator")
        void shouldParseAllEqualsOperator() {
            var tokens = condition(
                // Case insensitive
                text("field1"), operator("="), quotedString("a"), text("AND"),
                text("field2"), operator("=="), quotedString("b"), text("AND"),
                text("field3"), text("EQUALS_IC"), quotedString("c"), text("AND"),
                // Case sensitive
                text("field4"), operator("==="), quotedString("d"), text("AND"),
                text("field5"), text("EQUALS"), quotedString("e")
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
            var tokens = condition(text("field1"), text("EQUALS"), number("1"));

            final TokensParser parser = new TokensParser();
            assertThat(parser.parse(tokens), equalTo(new ConditionNode[]{
                criteria("field1", "EQUALS", 1).build(),
            }));
        }

        @Test
        @DisplayName("should reject other values")
        void shouldRejectOtherValues() {
            var tokens = condition(
                text("field1"), operator("="), text("abc") // Non-quoted string and non-numeric
            );

            final TokensParser parser = new TokensParser();
            final SyntaxException exception = Assertions.assertThrows(SyntaxException.class,
                                                                      () -> parser.parse(tokens));
            assertThat(
                exception.getMessage(),
                equalTo("Unexpected token TextToken[value=abc], expected one of: VALUE_NUMBER, VALUE_STRING")
            );
        }
    }

    @Nested
    @DisplayName("STARTS_WITH")
    class StartsWithOperator {
        @Test
        @DisplayName("should parse all forms of STARTS_WITH operator")
        void shouldParseAllStartsWithOperator() {
            var tokens = condition(
                    // Case insensitive
                    text("field1"), text("STARTS_WITH_IC"), quotedString("a"), text("AND"),
                    // Case sensitive
                    text("field2"), text("STARTS_WITH"), quotedString("b")
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
            var tokens = condition(
                    text("field1"), text("STARTS_WITH"), text("123")
            );

            final TokensParser parser = new TokensParser();
            final SyntaxException exception = Assertions.assertThrows(SyntaxException.class,
                                                                      () -> parser.parse(tokens));
            assertThat(
                exception.getMessage(),
                equalTo("Unexpected token TextToken[value=123], expected one of: VALUE_STRING")
            );
        }
    }

    @Nested
    @DisplayName("ENDS_WITH")
    class EndsWithOperator {
        @Test
        @DisplayName("should parse all forms of ENDS_WITH operator")
        void shouldParseAllEndsWithOperator() {
            var tokens = condition(
                    // Case insensitive
                    text("field1"), text("ENDS_WITH_IC"), quotedString("a"), text("AND"),
                    // Case sensitive
                    text("field2"), text("ENDS_WITH"), quotedString("b")
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
            var tokens = condition(
                    text("field1"), text("ENDS_WITH"), text("123")
            );

            final TokensParser parser = new TokensParser();
            final SyntaxException exception = Assertions.assertThrows(SyntaxException.class,
                                                                      () -> parser.parse(tokens));
            assertThat(
                exception.getMessage(),
                equalTo("Unexpected token TextToken[value=123], expected one of: VALUE_STRING")
            );
        }
    }

    @Nested
    @DisplayName("CONTAINS")
    class ContainsOperator {
        @Test
        @DisplayName("should parse all forms of CONTAINS operator")
        void shouldParseAllContainsOperator() {
            var tokens = condition(
                    // Case insensitive
                    text("field1"), text("CONTAINS_IC"), quotedString("a"), text("AND"),
                    // Case sensitive
                    text("field2"), text("CONTAINS"), quotedString("b")
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
            var tokens = condition(text("field1"), text("CONTAINS"), number("1"));

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
            var tokens = condition(
                    text("field1"), text("MATCHES"), quotedString("^[a-z]{3}$")
            );

            final TokensParser parser = new TokensParser();
            assertThat(parser.parse(tokens), equalTo(new ConditionNode[]{
                criteria("field1", "MATCHES", "^[a-z]{3}$").build(),
            }));
        }

        @Test
        @DisplayName("should reject numeric values")
        void shouldRejectNumericValues() {
            var tokens = condition(
                    text("field1"), text("MATCHES"), text("123")
            );

            final TokensParser parser = new TokensParser();
            final SyntaxException exception = Assertions.assertThrows(SyntaxException.class,
                                                                      () -> parser.parse(tokens));
            assertThat(
                exception.getMessage(),
                equalTo("Unexpected token TextToken[value=123], expected one of: VALUE_STRING")
            );
        }
    }

    @Nested
    @DisplayName("HAS_LENGTH")
    class HasLengthOperator {
        @Test
        @DisplayName("should parse HAS_LENGTH operator")
        void shouldParseAllContainsOperator() {
            var tokens = condition(text("field1"), text("HAS_LENGTH"), number("3"));

            final TokensParser parser = new TokensParser();
            assertThat(parser.parse(tokens), equalTo(new ConditionNode[]{
                criteria("field1", "HAS_LENGTH", 3).build(),
            }));
        }

        @Test
        @DisplayName("should reject quoted string values")
        void shouldRejectQuotedStringValues() {
            var tokens = condition(text("field1"), text("HAS_LENGTH"), quotedString("abc"));

            final TokensParser parser = new TokensParser();
            final SyntaxException exception = Assertions.assertThrows(SyntaxException.class,
                                                                      () -> parser.parse(tokens));
            assertThat(
                exception.getMessage(),
                equalTo("Unexpected token QuotedStringToken[value=abc], expected one of: VALUE_NUMBER")
            );
        }
    }

    @Nested
    @DisplayName("GREATER_THAN")
    class GreaterThanOperator {
        @Test
        @DisplayName("should parse all forms of GREATER_THAN operator")
        void shouldParseAllEqualsOperator() {
            var tokens = condition(
                    text("field1"), operator(">"), number("2"), text("AND"),
                    text("field2"), text("GREATER_THAN"), number("2")
            );

            final TokensParser parser = new TokensParser();
            assertThat(parser.parse(tokens), equalTo(new ConditionNode[]{
                    criteria("field1", "GREATER_THAN", 2).build(),
                    AND,
                    criteria("field2", "GREATER_THAN", 2).build(),
            }));
        }

        @Test
        @DisplayName("should reject text values")
        void shouldRejectTextValues() {
            var tokens = condition(
                    text("field1"), operator(">"), text("abc") // Non-quoted string
            );

            final TokensParser parser = new TokensParser();
            final SyntaxException exception = Assertions.assertThrows(SyntaxException.class,
                    () -> parser.parse(tokens));
            assertThat(
                    exception.getMessage(),
                    equalTo("Unexpected token TextToken[value=abc], expected one of: VALUE_NUMBER")
            );
        }

        @Test
        @DisplayName("should reject quoted string values")
        void shouldRejectQuotedStringValues() {
            var tokens = condition(
                    text("field2"), operator(">"), quotedString("abc") // Quoted string
            );

            final TokensParser parser = new TokensParser();
            final SyntaxException exception = Assertions.assertThrows(SyntaxException.class,
                    () -> parser.parse(tokens));
            assertThat(
                    exception.getMessage(),
                    equalTo("Unexpected token QuotedStringToken[value=abc], expected one of: VALUE_NUMBER")
            );
        }
    }

    @Nested
    @DisplayName("GREATER_OR_EQUALS")
    class GreaterOrEqualsOperator {
        @Test
        @DisplayName("should parse all forms of GREATER_OR_EQUALS operator")
        void shouldParseAllEqualsOperator() {
            var tokens = condition(
                    text("field1"), operator(">="), number("2"), text("AND"),
                    text("field2"), text("GREATER_OR_EQUALS"), number("2")
            );

            final TokensParser parser = new TokensParser();
            assertThat(parser.parse(tokens), equalTo(new ConditionNode[]{
                    criteria("field1", "GREATER_OR_EQUALS", 2).build(),
                    AND,
                    criteria("field2", "GREATER_OR_EQUALS", 2).build(),
            }));
        }

        @Test
        @DisplayName("should reject text values")
        void shouldRejectTextValues() {
            var tokens = condition(
                    text("field1"), operator(">="), text("abc") // Non-quoted string
            );

            final TokensParser parser = new TokensParser();
            final SyntaxException exception = Assertions.assertThrows(SyntaxException.class,
                    () -> parser.parse(tokens));
            assertThat(
                    exception.getMessage(),
                    equalTo("Unexpected token TextToken[value=abc], expected one of: VALUE_NUMBER")
            );
        }

        @Test
        @DisplayName("should reject quoted string values")
        void shouldRejectQuotedStringValues() {
            var tokens = condition(
                    text("field2"), operator(">="), quotedString("abc") // Quoted string
            );

            final TokensParser parser = new TokensParser();
            final SyntaxException exception = Assertions.assertThrows(SyntaxException.class,
                    () -> parser.parse(tokens));
            assertThat(
                    exception.getMessage(),
                    equalTo("Unexpected token QuotedStringToken[value=abc], expected one of: VALUE_NUMBER")
            );
        }
    }

    @Nested
    @DisplayName("LESS_THAN")
    class LessThanOperator {
        @Test
        @DisplayName("should parse all forms of LESS_THAN operator")
        void shouldParseAllEqualsOperator() {
            var tokens = condition(
                    text("field1"), operator("<"), number("2"), text("AND"),
                    text("field2"), text("LESS_THAN"), number("2")
            );

            final TokensParser parser = new TokensParser();
            assertThat(parser.parse(tokens), equalTo(new ConditionNode[]{
                    criteria("field1", "LESS_THAN", 2).build(),
                    AND,
                    criteria("field2", "LESS_THAN", 2).build(),
            }));
        }

        @Test
        @DisplayName("should reject text values")
        void shouldRejectTextValues() {
            var tokens = condition(
                    text("field1"), operator("<"), text("abc") // Non-quoted string
            );

            final TokensParser parser = new TokensParser();
            final SyntaxException exception = Assertions.assertThrows(SyntaxException.class,
                    () -> parser.parse(tokens));
            assertThat(
                    exception.getMessage(),
                    equalTo("Unexpected token TextToken[value=abc], expected one of: VALUE_NUMBER")
            );
        }

        @Test
        @DisplayName("should reject quoted string values")
        void shouldRejectQuotedStringValues() {
            var tokens = condition(
                    text("field2"), operator("<"), quotedString("abc") // Quoted string
            );

            final TokensParser parser = new TokensParser();
            final SyntaxException exception = Assertions.assertThrows(SyntaxException.class,
                    () -> parser.parse(tokens));
            assertThat(
                    exception.getMessage(),
                    equalTo("Unexpected token QuotedStringToken[value=abc], expected one of: VALUE_NUMBER")
            );
        }
    }

    @Nested
    @DisplayName("LESS_OR_EQUALS")
    class LessOrEqualsOperator {
        @Test
        @DisplayName("should parse all forms of LESS_OR_EQUALS operator")
        void shouldParseAllEqualsOperator() {
            var tokens = condition(
                    text("field1"), operator("<="), number("2"), text("AND"),
                    text("field2"), text("LESS_OR_EQUALS"), number("2")
            );

            final TokensParser parser = new TokensParser();
            assertThat(parser.parse(tokens), equalTo(new ConditionNode[]{
                    criteria("field1", "LESS_OR_EQUALS", 2).build(),
                    AND,
                    criteria("field2", "LESS_OR_EQUALS", 2).build(),
            }));
        }

        @Test
        @DisplayName("should reject text values")
        void shouldRejectTextValues() {
            var tokens = condition(
                    text("field1"), operator("<="), text("abc") // Non-quoted string
            );

            final TokensParser parser = new TokensParser();
            final SyntaxException exception = Assertions.assertThrows(SyntaxException.class,
                    () -> parser.parse(tokens));
            assertThat(
                    exception.getMessage(),
                    equalTo("Unexpected token TextToken[value=abc], expected one of: VALUE_NUMBER")
            );
        }

        @Test
        @DisplayName("should reject quoted string values")
        void shouldRejectQuotedStringValues() {
            var tokens = condition(
                    text("field2"), operator("<="), quotedString("abc") // Quoted string
            );

            final TokensParser parser = new TokensParser();
            final SyntaxException exception = Assertions.assertThrows(SyntaxException.class,
                    () -> parser.parse(tokens));
            assertThat(
                    exception.getMessage(),
                    equalTo("Unexpected token QuotedStringToken[value=abc], expected one of: VALUE_NUMBER")
            );
        }
    }
}
