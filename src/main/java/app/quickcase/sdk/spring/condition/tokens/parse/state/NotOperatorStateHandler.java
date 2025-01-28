package app.quickcase.sdk.spring.condition.tokens.parse.state;

import app.quickcase.sdk.spring.condition.tokens.TextToken;
import app.quickcase.sdk.spring.condition.tokens.Token;
import app.quickcase.sdk.spring.condition.tokens.parse.ParsingContext;

import static app.quickcase.sdk.spring.condition.tokens.parse.state.ParsingState.FIELD_PATH;
import static app.quickcase.sdk.spring.condition.tokens.parse.state.ParsingState.GROUP_START;

class NotOperatorStateHandler implements ParsingStateHandler {
    private static final TextToken NOT = new TextToken("NOT");

    @Override
    public Boolean accept(Token token) {
        return NOT.equals(token);
    }

    @Override
    public ParsingState[] nextStates(ParsingContext context) {
        return new ParsingState[] {GROUP_START, FIELD_PATH};
    }

    @Override
    public void apply(ParsingContext context, Token token) {
        context.negateNext();
    }
}
