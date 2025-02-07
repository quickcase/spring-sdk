package app.quickcase.sdk.spring.condition.tokens.extract;

import java.util.Arrays;

public enum TokenCharacter {
    // Digits
    ZERO('0'),
    NINE('9'),
    // Uppercase letters
    UPPER_A('A'),
    UPPER_Z('Z'),
    // Lowercase letters
    LOWER_A('a'),
    LOWER_Z('z'),
    // Symbols
    DOUBLE_QUOTE('"'),
    PARENTHESIS_OPEN('('),
    PARENTHESIS_CLOSE(')'),
    COLON(':'),
    DOT('.'),
    EQUAL('='),
    GREATER_THAN('>'),
    LESS_THAN('<'),
    SQUARE_BRACKET_OPEN('['),
    SQUARE_BRACKET_CLOSE(']'),
    UNDERSCORE('_');

    // ASCII code
    public final int code;

    TokenCharacter(char character) {
        this.code = character;
    }

    public static final TokenCharacter[] OPERATOR_SYMBOLS = new TokenCharacter[]{
        EQUAL,
        GREATER_THAN,
        LESS_THAN,
    };

    public static final TokenCharacter[] GROUP_DELIMITERS = new TokenCharacter[]{
        PARENTHESIS_OPEN,
        PARENTHESIS_CLOSE,
    };

    public static Boolean isText(int code) {
        if (code >= ZERO.code && code <= NINE.code) {
            // 0-9
            return true;
        }
        if (code >= UPPER_A.code && code <= UPPER_Z.code) {
            // A-Z
            return true;
        }
        if (code >= LOWER_A.code && code <= LOWER_Z.code) {
            // a-z
            return true;
        }
        if (code == COLON.code || code == DOT.code || code == UNDERSCORE.code || code == SQUARE_BRACKET_OPEN.code || code == SQUARE_BRACKET_CLOSE.code) {
            // :._[]
            return true;
        }

        return false;
    }

    public static Boolean isDigit(int code) {
        return code >= ZERO.code && code <= NINE.code;
    }

    public static Boolean isDoubleQuote(int code) {
        return code == DOUBLE_QUOTE.code;
    }

    public static Boolean isOperatorSymbol(int code) {
        return Arrays.stream(OPERATOR_SYMBOLS).anyMatch((symbol) -> symbol.code == code);
    }

    public static Boolean isGroupDelimiter(int code) {
        return Arrays.stream(GROUP_DELIMITERS).anyMatch((symbol) -> symbol.code == code);
    }
}
