package app.quickcase.sdk.spring.condition.tokens.parse.state;

import app.quickcase.sdk.spring.condition.tokens.Token;
import app.quickcase.sdk.spring.condition.tokens.parse.ParsingContext;

/**
 * Enum of all the valid state and transition that parsing of a condition can follow.
 */
public enum ParsingState {
    VALUE_NUMBER(new IntegerValueStateHandler()),
    VALUE_STRING(new StringValueStateHandler()),
    BINARY_OPERATOR(new BinaryOperatorStateHandler()),
    COMP_CONTAINS(new CaseSensitiveOperatorStateHandler(
        "CONTAINS",
        new String[] {"CONTAINS"},
        new String[] {"CONTAINS_IC"},
        new ParsingState[]{VALUE_NUMBER, VALUE_STRING}
    )),
    COMP_EQUALS(new CaseSensitiveOperatorStateHandler(
        "EQUALS",
        new String[] {"===", "EQUALS"},
        new String[] {"=", "==", "EQUALS_IC"},
        new ParsingState[]{VALUE_NUMBER, VALUE_STRING}
    )),
    COMP_ENDS_WITH(new CaseSensitiveOperatorStateHandler(
        "ENDS_WITH",
        new String[] {"ENDS_WITH"},
        new String[] {"ENDS_WITH_IC"},
        new ParsingState[]{VALUE_STRING}
    )),
    COMP_GREATER_THAN(new OperatorStateHandler(
            "GREATER_THAN",
            new String[] {"GREATER_THAN", ">"},
            new ParsingState[]{VALUE_NUMBER}
    )),
    COMP_GREATER_OR_EQUALS(new OperatorStateHandler(
            "GREATER_OR_EQUALS",
            new String[] {"GREATER_OR_EQUALS", ">="},
            new ParsingState[]{VALUE_NUMBER}
    )),
    COMP_HAS_LENGTH(new OperatorStateHandler(
        new String[]{"HAS_LENGTH"},
        new ParsingState[]{VALUE_NUMBER}
    )),
    COMP_LESS_THAN(new OperatorStateHandler(
            "LESS_THAN",
            new String[] {"LESS_THAN", "<"},
            new ParsingState[]{VALUE_NUMBER}
    )),
    COMP_LESS_OR_EQUALS(new OperatorStateHandler(
            "LESS_OR_EQUALS",
            new String[] {"LESS_OR_EQUALS", "<="},
            new ParsingState[]{VALUE_NUMBER}
    )),
    COMP_MATCHES(new OperatorStateHandler(
        new String[]{"MATCHES"},
        new ParsingState[]{VALUE_STRING}
    )),
    COMP_STARTS_WITH(new CaseSensitiveOperatorStateHandler(
        "STARTS_WITH",
        new String[] {"STARTS_WITH"},
        new String[] {"STARTS_WITH_IC"},
        new ParsingState[]{VALUE_STRING}
    )),
    END(new EndStateHandler()),
    FIELD_PATH(new FieldPathStateHandler()),
    GROUP_END(new GroupEndStateHandler()),
    GROUP_START(new GroupStartStateHandler()),
    NOT_OPERATOR(new NotOperatorStateHandler()),
    START(new StartStateHandler());

    static final ParsingState[] COMPARISON_OPERATORS = {
        COMP_CONTAINS,
        COMP_EQUALS,
        COMP_ENDS_WITH,
        COMP_GREATER_THAN,
        COMP_GREATER_OR_EQUALS,
        COMP_HAS_LENGTH,
        COMP_LESS_THAN,
        COMP_LESS_OR_EQUALS,
        COMP_MATCHES,
        COMP_STARTS_WITH,
    };

    ParsingState(ParsingStateHandler handler) {
        this.handler = handler;
    }

    private final ParsingStateHandler handler;

    public Boolean accept(Token token) {
        return this.handler.accept(token);
    }

    public void apply(ParsingContext context, Token token) {
        this.handler.apply(context, token);
    }

    public ParsingState[] nextStates(ParsingContext context) {
        return this.handler.nextStates(context);
    }
}
