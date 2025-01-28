package app.quickcase.sdk.spring.condition.tokens.parse.state;

import app.quickcase.sdk.spring.condition.Criteria;
import app.quickcase.sdk.spring.condition.tokens.NumberToken;
import app.quickcase.sdk.spring.condition.tokens.Token;
import app.quickcase.sdk.spring.condition.tokens.parse.ParsingContext;

import static app.quickcase.sdk.spring.condition.tokens.parse.state.ParsingState.*;

class IntegerValueStateHandler implements ParsingStateHandler {

    @Override
    public Boolean accept(Token token) {
        return token instanceof NumberToken;
    }

    @Override
    public ParsingState[] nextStates(ParsingContext context) {
        if (context.insideNestedGroup()) {
            return new ParsingState[] {BINARY_OPERATOR, GROUP_END};
        }

        return new ParsingState[] {BINARY_OPERATOR, END};
    }

    @Override
    public void apply(ParsingContext context, Token token) {
        final Criteria.CriteriaBuilder criteria = context.getCriteriaBuilder()
                                                         .value(Integer.parseInt(token.value()));
        context.getCurrentGroup()
               .addCriteria(criteria);
    }
}
