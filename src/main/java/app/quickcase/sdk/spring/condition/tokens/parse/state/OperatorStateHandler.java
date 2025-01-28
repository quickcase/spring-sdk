package app.quickcase.sdk.spring.condition.tokens.parse.state;

import java.util.Arrays;

import app.quickcase.sdk.spring.condition.tokens.parse.ParsingContext;

class OperatorStateHandler implements ParsingStateHandler {
    private final String[] acceptTokens;
    private final ParsingState[] nextStates;

    public OperatorStateHandler(String[] acceptTokens, ParsingState[] nextStates) {
        this.acceptTokens = acceptTokens;
        this.nextStates = nextStates;
    }

    @Override
    public Boolean accept(String token) {
        return Arrays.asList(acceptTokens).contains(token);
    }

    @Override
    public ParsingState[] nextStates(ParsingContext context) {
        return this.nextStates;
    }

    @Override
    public void apply(ParsingContext context, String token) {
        context.getCriteriaBuilder().operator(token);
    }
}
