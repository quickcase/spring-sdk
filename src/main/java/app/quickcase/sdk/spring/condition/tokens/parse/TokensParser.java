package app.quickcase.sdk.spring.condition.tokens.parse;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import app.quickcase.sdk.spring.condition.ConditionNode;
import app.quickcase.sdk.spring.condition.tokens.parse.error.SyntaxException;
import app.quickcase.sdk.spring.condition.tokens.parse.state.ParsingState;

/**
 * Second step of condition parsing: Given a sequential list of tokens, validate the nature and order of the tokens
 * (the syntax) and group tokens together into meaningful elements.
 * <br>
 * For example, the list of tokens {@code field1}, {@code ===}, {@code "Value 1"} would be parsed into a single criteria
 * of type {@code EQUALS} with path {@code field1} and value {@code "Value 1"}.
 */
public class TokensParser {

    public ConditionNode[] parse(String[] tokens) {
        ParsingState state = ParsingState.START;
        ParsingContext context = new ParsingContext();

        for (final String token : tokens) {
            final ParsingState[] nextPossibleStates = state.nextStates(context);
            final Optional<ParsingState> nextState = Arrays.stream(nextPossibleStates)
                                                           .filter((posState) -> posState.accept(token))
                                                           .findFirst();
            if (nextState.isEmpty()) {
                throw new SyntaxException(String.format(
                    "Unexpected token '%s', expected one of: %s",
                    token,
                    formatStates(nextPossibleStates)
                ));
            }

            state = nextState.get();
            state.apply(context, token);
        }

        // Validate final state
        final ParsingState[] endStates = state.nextStates(context);
        if (!Arrays.asList(endStates).contains(ParsingState.END)) {
            throw new SyntaxException("Unexpected end of condition, expected one of: " + formatStates(endStates));
        }

        return context.rootNodes();
    }

    private String formatStates(ParsingState[] states) {
        return Arrays.stream(states).map(Enum::name).collect(Collectors.joining(", "));
    }
}
