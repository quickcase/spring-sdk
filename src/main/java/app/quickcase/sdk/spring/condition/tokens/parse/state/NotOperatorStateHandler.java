package app.quickcase.sdk.spring.condition.tokens.parse.state;

import app.quickcase.sdk.spring.condition.tokens.parse.ParsingContext;

import static app.quickcase.sdk.spring.condition.tokens.parse.state.ParsingState.FIELD_PATH;
import static app.quickcase.sdk.spring.condition.tokens.parse.state.ParsingState.GROUP_START;

class NotOperatorStateHandler implements ParsingStateHandler {

    @Override
    public Boolean accept(String token) {
        return "NOT".equals(token);
    }

    @Override
    public ParsingState[] nextStates(ParsingContext context) {
        return new ParsingState[] {GROUP_START, FIELD_PATH};
    }

    @Override
    public void apply(ParsingContext context, String token) {
        context.negateNext();
    }
}
