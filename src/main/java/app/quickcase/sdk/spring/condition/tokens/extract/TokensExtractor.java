package app.quickcase.sdk.spring.condition.tokens.extract;

import java.util.ArrayDeque;
import java.util.stream.Stream;

import app.quickcase.sdk.spring.condition.tokens.GroupToken;
import app.quickcase.sdk.spring.condition.tokens.OperatorToken;
import app.quickcase.sdk.spring.condition.tokens.QuotedStringToken;
import app.quickcase.sdk.spring.condition.tokens.TextToken;
import app.quickcase.sdk.spring.condition.tokens.Token;
import app.quickcase.sdk.spring.condition.tokens.VoidToken;

import static app.quickcase.sdk.spring.condition.tokens.TokenCharacter.*;

/**
 * First step of condition parsing: Given a raw string, identify and separate the relevant atomic elements (the tokens).
 *
 * During this phase, formatting and unexpected characters (eg. whitepaces, line breaks) are removed.
 */
public class TokensExtractor {

    public String[] extract(String conditionString) {
        final Stream<Character> characters = conditionString.trim().chars().mapToObj(c -> (char) c);

        final ArrayDeque<Token> tokens = new ArrayDeque<>();
        tokens.add(new VoidToken()); // Initialise with void to avoid NoSuchElementException

        characters.forEach((character) -> {
            if (tokens.getLast().append(character)) {
                // Character appended to current token
                return;
            }

            if (isText(character)) {
                tokens.addLast(new TextToken(character));
            } else if (isOperatorSymbol(character)) {
                tokens.addLast(new OperatorToken(character));
            } else if (isDoubleQuote(character)) {
                tokens.addLast(new QuotedStringToken(character));
            } else if (isGroupDelimiter(character)) {
                tokens.addLast(new GroupToken(character));
            } else {
                // Else ignore character
                tokens.addLast(new VoidToken());
            }
        });

        return tokens.stream()
                     .filter((token) -> !(token instanceof VoidToken))
                     .map(Token::getValue)
                     .toArray(String[]::new);
    }
}
