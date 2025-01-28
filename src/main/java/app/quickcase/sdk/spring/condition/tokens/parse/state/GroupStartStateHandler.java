package app.quickcase.sdk.spring.condition.tokens.parse.state;

import app.quickcase.sdk.spring.condition.tokens.parse.ParsingContext;

import static app.quickcase.sdk.spring.condition.tokens.parse.state.ParsingState.*;

class GroupStartStateHandler implements ParsingStateHandler {

    @Override
    public Boolean accept(String token) {
        return "(".equals(token);
    }

    @Override
    public ParsingState[] nextStates(ParsingContext context) {
        return new ParsingState[] {GROUP_START, NOT_OPERATOR, FIELD_PATH};
    }

    @Override
    public void apply(ParsingContext context, String token) {
        context.newGroup();
    }
}
