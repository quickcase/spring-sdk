package app.quickcase.sdk.spring.condition.tokens.parse.state;

import app.quickcase.sdk.spring.condition.tokens.Token;
import app.quickcase.sdk.spring.condition.tokens.parse.ParsingContext;

class EndStateHandler implements ParsingStateHandler {
    @Override
    public Boolean accept(Token token) {
        return false;
    }

    @Override
    public ParsingState[] nextStates(ParsingContext context) {
        return new ParsingState[0];
    }

    @Override
    public void apply(ParsingContext context, Token token) {
        throw new RuntimeException("End state does not accept tokens");
    }
}
