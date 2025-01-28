package app.quickcase.sdk.spring.condition.tokens.parse.state;

import app.quickcase.sdk.spring.condition.tokens.parse.ParsingContext;

import static app.quickcase.sdk.spring.condition.tokens.parse.state.ParsingState.*;

class GroupEndStateHandler implements ParsingStateHandler {

    @Override
    public Boolean accept(String token) {
        return ")".equals(token);
    }

    @Override
    public ParsingState[] nextStates(ParsingContext context) {
        if (context.insideNestedGroup()) {
            return new ParsingState[] {BINARY_OPERATOR, GROUP_END};
        }

        return new ParsingState[] {BINARY_OPERATOR, END};
    }

    @Override
    public void apply(ParsingContext context, String token) {
        context.closeCurrentGroup();
    }
}
