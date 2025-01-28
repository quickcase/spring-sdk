package app.quickcase.sdk.spring.condition.tokens.parse.state;

import app.quickcase.sdk.spring.condition.tokens.Token;
import app.quickcase.sdk.spring.condition.tokens.parse.ParsingContext;

import static app.quickcase.sdk.spring.condition.tokens.parse.state.ParsingState.*;

class StartStateHandler implements ParsingStateHandler {

    @Override
    public Boolean accept(Token token) {
        return false;
    }

    @Override
    public ParsingState[] nextStates(ParsingContext context) {
        return new ParsingState[] {GROUP_START, NOT_OPERATOR, FIELD_PATH};
    }

    @Override
    public void apply(ParsingContext context, Token token) {
        throw new RuntimeException("Start state does not accept tokens");
    }
}
