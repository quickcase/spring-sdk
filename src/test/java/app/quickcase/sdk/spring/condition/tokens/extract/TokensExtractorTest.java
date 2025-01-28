package app.quickcase.sdk.spring.condition.tokens.extract;

import java.util.stream.Stream;

import app.quickcase.sdk.spring.condition.tokens.extract.TokensExtractor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class TokensExtractorTest {

    private static Arguments args(String name, String condition, String[] expected) {
        return Arguments.of(name, condition, expected);
    }

    private static String[] array(String... items) {
        return items;
    }

    private static Stream<Arguments> provideHappyTestCases() {
        return Stream.of(
            args(
                "Legacy condition without parenthesis",
                "field1=\"value1\" AND field2=\"Yes\"",
                array(
                    "field1", "=", "\"value1\"",
                    "AND",
                    "field2", "=", "\"Yes\""
                )
            ),
            args(
                "Composed condition with operator symbols",
                "(a = 1 AND b === 2) OR (c=2 AND d===4)",
                array(
                    "(", "a", "=", "1", "AND", "b", "===", "2", ")",
                    "OR",
                    "(", "c", "=", "2", "AND", "d", "===", "4", ")"
                )
            ),
            args(
                "Complex field names",
                "complexField.child1 = \"Yes\" AND prefix_field2 = \"No\"",
                array(
                    "complexField.child1", "=", "\"Yes\"",
                    "AND",
                    "prefix_field2", "=", "\"No\""
                )
            ),
            args(
                "Quoted strings",
                "field1 = \"Value with space\" OR field2 = \"#ValueWithSpecial(Chars)!\"",
                array(
                    "field1", "=", "\"Value with space\"",
                    "OR",
                    "field2", "=", "\"#ValueWithSpecial(Chars)!\""
                )
            ),
            args(
                "Leading, middling and trailing spaces",
                "   field1   =   1   ",
                array("field1", "=", "1")
            ),
            args(
                "Negated condition",
                "(a = 1 AND NOT (b === 2)) OR NOT(NOT(c=2) AND d===4)",
                array(
                    "(", "a", "=", "1", "AND", "NOT", "(", "b", "===", "2", ")", ")",
                    "OR",
                    "NOT", "(", "NOT", "(", "c", "=", "2", ")", "AND", "d", "===", "4", ")"
                )
            ),
            args(
                "Condition formatted with line breaks",
                "(\n" +
                    "      a = 1 AND NOT (b === 2)\n" +
                    "    ) OR NOT(c===3)\n" +
                    "",
                array(
                    "(", "a", "=", "1", "AND", "NOT", "(", "b", "===", "2", ")", ")",
                    "OR",
                    "NOT", "(", "c", "===", "3", ")"
                )
            )
        );
    }

    @DisplayName("should parse condition string into tokens")
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideHappyTestCases")
    void shouldParseConditionStringIntoTokens(String name, String condition, String[] expected) {
        final TokensExtractor extractor = new TokensExtractor();

        assertThat(extractor.extract(condition), equalTo(expected));
    }
}
