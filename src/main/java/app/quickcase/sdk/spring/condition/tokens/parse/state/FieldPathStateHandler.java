package app.quickcase.sdk.spring.condition.tokens.parse.state;

import java.util.regex.Pattern;

import app.quickcase.sdk.spring.condition.tokens.TextToken;
import app.quickcase.sdk.spring.condition.tokens.Token;
import app.quickcase.sdk.spring.condition.tokens.parse.ParsingContext;

class FieldPathStateHandler implements ParsingStateHandler {
    private static final Pattern REGEX = Pattern.compile("^[a-zA-Z0-9._]+$");

    @Override
    public Boolean accept(Token token) {
        return token instanceof TextToken && REGEX.matcher(token.value()).matches();
    }

    @Override
    public ParsingState[] nextStates(ParsingContext context) {
        return ParsingState.COMPARISON_OPERATORS;
    }

    @Override
    public void apply(ParsingContext context, Token token) {
        context.newCriteria().path(token.value());
    }
}
