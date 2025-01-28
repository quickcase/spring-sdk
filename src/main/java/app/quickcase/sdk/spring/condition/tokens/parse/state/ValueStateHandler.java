package app.quickcase.sdk.spring.condition.tokens.parse.state;

import java.util.function.Function;
import java.util.regex.Pattern;

import app.quickcase.sdk.spring.condition.Criteria;
import app.quickcase.sdk.spring.condition.tokens.parse.ParsingContext;

import static app.quickcase.sdk.spring.condition.tokens.parse.state.ParsingState.*;

class ValueStateHandler implements ParsingStateHandler {
    public static final Function<String, Object> TO_UNQUOTED = token -> token.substring(1, token.length() - 1);
    public static final Function<String, Object> TO_INT = Integer::parseInt;

    public static final Pattern QUOTED_VALUE = Pattern.compile("^\"[^\"]*\"$");
    public static final Pattern NUMERIC_VALUE = Pattern.compile("^\\d+$");

    private final Pattern regex;
    private final Function<String, Object> valueTransform;

    ValueStateHandler(Pattern regex, Function<String, Object> valueTransform) {
        this.regex = regex;
        this.valueTransform = valueTransform;
    }

    @Override
    public Boolean accept(String token) {
        return this.regex.matcher(token).matches();
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
        final Criteria.CriteriaBuilder criteria = context.getCriteriaBuilder()
                                                         .value(valueTransform.apply(token));
        context.getCurrentGroup()
               .addCriteria(criteria);
    }
}
