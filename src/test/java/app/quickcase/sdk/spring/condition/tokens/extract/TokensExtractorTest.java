package app.quickcase.sdk.spring.condition.tokens.extract;

import java.util.stream.Stream;

import app.quickcase.sdk.spring.condition.tokens.Token;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static app.quickcase.sdk.spring.condition.tokens.Token.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class TokensExtractorTest {

    private static Arguments args(String name, String condition, Token[] expected) {
        return Arguments.of(name, condition, expected);
    }

    private static Token[] array(Token... items) {
        return items;
    }

    private static Stream<Arguments> provideHappyTestCases() {
        return Stream.of(
                args(
                        "Legacy condition without parenthesis",
                        "field1=\"value1\" AND field2=\"Yes\"",
                        array(
                                text("field1"), operator("="), quotedString("value1"),
                                text("AND"),
                                text("field2"), operator("="), quotedString("Yes")
                        )
                ),
                args(
                        "Composed condition with operator symbols",
                        "(a = 1 AND b === 2) OR (c=2 AND d===4)",
                        array(
                                groupDelimiter("("), text("a"), operator("="), number("1"), text("AND"), text("b"), operator("==="), number("2"), groupDelimiter(")"),
                                text("OR"),
                                groupDelimiter("("), text("c"), operator("="), number("2"), text("AND"), text("d"), operator("==="), number("4"), groupDelimiter(")")
                        )
                ),
                args(
                        "Complex field names",
                        "complexField.child1 = \"Yes\" AND prefix_field2 = \"No\"",
                        array(
                                text("complexField.child1"), operator("="), quotedString("Yes"),
                                text("AND"),
                                text("prefix_field2"), operator("="), quotedString("No")
                        )
                ),
                args(
                        "Quoted strings",
                        "field1 = \"Value with space\" OR field2 = \"#ValueWithSpecial(Chars)!\"",
                        array(
                                text("field1"), operator("="), quotedString("Value with space"),
                                text("OR"),
                                text("field2"), operator("="), quotedString("#ValueWithSpecial(Chars)!")
                        )
                ),
                args(
                        "Leading, middling and trailing spaces",
                        "   field1   =   1   ",
                        array(text("field1"), operator("="), number("1"))
                ),
                args(
                        "Negated condition",
                        "(a = 1 AND NOT (b === 2)) OR NOT(NOT(c=2) AND d===4)",
                        array(
                                groupDelimiter("("), text("a"), operator("="), number("1"), text("AND"), text("NOT"), groupDelimiter("("), text("b"), operator("==="), number("2"), groupDelimiter(")"), groupDelimiter(")"),
                                text("OR"),
                                text("NOT"), groupDelimiter("("), text("NOT"), groupDelimiter("("), text("c"), operator("="), number("2"), groupDelimiter(")"), text("AND"), text("d"), operator("==="), number("4"), groupDelimiter(")")
                        )
                ),
                args(
                        "Condition formatted with line breaks",
                        "(\n" +
                                "      a = 1 AND NOT (b === 2)\n" +
                                "    ) OR NOT(c===3)\n",
                        array(
                                groupDelimiter("("), text("a"), operator("="), number("1"), text("AND"), text("NOT"), groupDelimiter("("), text("b"), operator("==="), number("2"), groupDelimiter(")"), groupDelimiter(")"),
                                text("OR"),
                                text("NOT"), groupDelimiter("("), text("c"), operator("==="), number("3"), groupDelimiter(")")
                        )
                ),
                args(
                        "Support for all field path syntaxes",
                        "[state] == \"active\" " +
                                "AND :computedField == 2" +
                                "AND complexField.member1 == \"test\" " +
                                "AND collectionField[0].value == \"itemValue\" " +
                                "AND collectionField[id:item1] == \"itemValue\"",
                        array(
                                text("[state]"), operator("=="), quotedString("active"),
                                text("AND"), text(":computedField"), operator("=="), number("2"),
                                text("AND"), text("complexField.member1"), operator("=="), quotedString("test"),
                                text("AND"), text("collectionField[0].value"), operator("=="), quotedString("itemValue"),
                                text("AND"), text("collectionField[id:item1]"), operator("=="), quotedString("itemValue")
                        )
                )
        );
    }

    @DisplayName("should parse condition string into tokens")
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideHappyTestCases")
    void shouldParseConditionStringIntoTokens(String name, String condition, Token[] expected) {
        final TokensExtractor extractor = new TokensExtractor();

        assertThat(extractor.extract(condition), equalTo(expected));
    }
}
