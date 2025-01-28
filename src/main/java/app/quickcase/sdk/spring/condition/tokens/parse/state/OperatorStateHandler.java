package app.quickcase.sdk.spring.condition.tokens.parse.state;

import java.util.Arrays;

import app.quickcase.sdk.spring.condition.tokens.OperatorToken;
import app.quickcase.sdk.spring.condition.tokens.TextToken;
import app.quickcase.sdk.spring.condition.tokens.Token;
import app.quickcase.sdk.spring.condition.tokens.parse.ParsingContext;

class OperatorStateHandler implements ParsingStateHandler {
    private final String[] acceptTokens;
    private final ParsingState[] nextStates;

    public OperatorStateHandler(String[] acceptTokens, ParsingState[] nextStates) {
        this.acceptTokens = acceptTokens;
        this.nextStates = nextStates;
    }

    @Override
    public Boolean accept(Token token) {
        return (token instanceof TextToken || token instanceof OperatorToken)
                && Arrays.asList(acceptTokens).contains(token.value());
    }

    @Override
    public ParsingState[] nextStates(ParsingContext context) {
        return this.nextStates;
    }

    @Override
    public void apply(ParsingContext context, Token token) {
        context.getCriteriaBuilder().operator(token.value());
    }
}
