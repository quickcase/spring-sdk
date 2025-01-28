package app.quickcase.sdk.spring.condition.tokens.parse.state;

import app.quickcase.sdk.spring.condition.BinaryOperator;
import app.quickcase.sdk.spring.condition.tokens.TextToken;
import app.quickcase.sdk.spring.condition.tokens.Token;
import app.quickcase.sdk.spring.condition.tokens.parse.ParsingContext;

import static app.quickcase.sdk.spring.condition.tokens.parse.state.ParsingState.*;

class BinaryOperatorStateHandler implements ParsingStateHandler {
    private static final TextToken AND = new TextToken("AND");
    private static final TextToken OR = new TextToken("OR");

    @Override
    public Boolean accept(Token token) {
        return AND.equals(token) || OR.equals(token);
    }

    @Override
    public ParsingState[] nextStates(ParsingContext context) {
        return new ParsingState[] {GROUP_START, NOT_OPERATOR, FIELD_PATH};
    }

    @Override
    public void apply(ParsingContext context, Token token) {
        context.getCurrentGroup()
               .addBinaryOperator(BinaryOperator.valueOf(token.value()));
    }
}
