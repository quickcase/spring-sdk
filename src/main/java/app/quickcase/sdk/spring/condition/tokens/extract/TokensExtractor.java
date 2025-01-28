package app.quickcase.sdk.spring.condition.tokens.extract;

import java.util.ArrayDeque;
import java.util.stream.Stream;

import app.quickcase.sdk.spring.condition.tokens.Token;
import app.quickcase.sdk.spring.condition.tokens.VoidToken;

import static app.quickcase.sdk.spring.condition.tokens.extract.TokenCharacter.*;

/**
 * First step of condition parsing: Given a raw string, identify and separate the relevant atomic elements (the tokens).
 *
 * During this phase, formatting and unexpected characters (eg. whitepaces, line breaks) are removed.
 */
public class TokensExtractor {

    public Token[] extract(String conditionString) {
        final Stream<Character> characters = conditionString.trim().chars().mapToObj(c -> (char) c);

        final ArrayDeque<TokenBuilder> builders = new ArrayDeque<>();
        builders.add(new VoidTokenBuilder()); // Initialise with void to avoid NoSuchElementException

        characters.forEach((character) -> {
            if (builders.getLast().append(character)) {
                // Character appended to current token
                return;
            }

            if (isDigit(character)) {
                builders.addLast(new NumberTokenBuilder(character));
            } else if (isText(character)) {
                builders.addLast(new TextTokenBuilder(character));
            } else if (isOperatorSymbol(character)) {
                builders.addLast(new OperatorTokenBuilder(character));
            } else if (isDoubleQuote(character)) {
                builders.addLast(new QuotedStringTokenBuilder(character));
            } else if (isGroupDelimiter(character)) {
                builders.addLast(new GroupDelimiterTokenBuilder(character));
            } else {
                // Else ignore character
                builders.addLast(new VoidTokenBuilder());
            }
        });

        return builders
                .stream()
                .map(TokenBuilder::build)
                .filter((token) -> !(token instanceof VoidToken))
                .toArray(Token[]::new);
    }
}
