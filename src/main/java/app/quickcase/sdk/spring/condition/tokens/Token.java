package app.quickcase.sdk.spring.condition.tokens;

public interface Token {
    String value();

    static GroupDelimiterToken groupDelimiter(String delimiter) {
        return new GroupDelimiterToken(delimiter);
    }

    static NumberToken number(String number) {
        return new NumberToken(number);
    }

    static OperatorToken operator(String operator) {
        return new OperatorToken(operator);
    }

    static QuotedStringToken quotedString(String quotedString) {
        return new QuotedStringToken(quotedString);
    }

    static TextToken text(String text) {
        return new TextToken(text);
    }
}
