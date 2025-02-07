package app.quickcase.sdk.spring.condition.tokens.parse.state;

import java.util.regex.Pattern;

import app.quickcase.sdk.spring.condition.tokens.TextToken;
import app.quickcase.sdk.spring.condition.tokens.Token;
import app.quickcase.sdk.spring.condition.tokens.parse.ParsingContext;
import app.quickcase.sdk.spring.path.FieldPath;

class FieldPathStateHandler implements ParsingStateHandler {
    @Override
    public Boolean accept(Token token) {
        return token instanceof TextToken && FieldPath.accepts(token.value());
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
