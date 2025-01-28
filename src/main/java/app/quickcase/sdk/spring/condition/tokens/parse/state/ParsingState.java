package app.quickcase.sdk.spring.condition.tokens.parse.state;

import app.quickcase.sdk.spring.condition.tokens.parse.ParsingContext;

import static app.quickcase.sdk.spring.condition.tokens.parse.state.ValueStateHandler.*;

/**
 * Enum of all the valid state and transition that parsing of a condition can follow.
 */
public enum ParsingState {
    VALUE_NUMBER(new ValueStateHandler(NUMERIC_VALUE, TO_INT)),
    VALUE_QUOTED(new ValueStateHandler(QUOTED_VALUE, TO_UNQUOTED)),
    BINARY_OPERATOR(new BinaryOperatorStateHandler()),
    COMP_CONTAINS(new CaseSensitiveOperatorStateHandler(
        "CONTAINS",
        new String[] {"CONTAINS"},
        new String[] {"CONTAINS_IC"},
        new ParsingState[]{VALUE_NUMBER, VALUE_QUOTED}
    )),
    COMP_EQUALS(new CaseSensitiveOperatorStateHandler(
        "EQUALS",
        new String[] {"===", "EQUALS"},
        new String[] {"=", "==", "EQUALS_IC"},
        new ParsingState[]{VALUE_NUMBER, VALUE_QUOTED}
    )),
    COMP_ENDS_WITH(new CaseSensitiveOperatorStateHandler(
        "ENDS_WITH",
        new String[] {"ENDS_WITH"},
        new String[] {"ENDS_WITH_IC"},
        new ParsingState[]{VALUE_QUOTED}
    )),
    COMP_HAS_LENGTH(new OperatorStateHandler(
        new String[]{"HAS_LENGTH"},
        new ParsingState[]{VALUE_NUMBER}
    )),
    COMP_MATCHES(new OperatorStateHandler(
        new String[]{"MATCHES"},
        new ParsingState[]{VALUE_QUOTED}
    )),
    COMP_STARTS_WITH(new CaseSensitiveOperatorStateHandler(
        "STARTS_WITH",
        new String[] {"STARTS_WITH"},
        new String[] {"STARTS_WITH_IC"},
        new ParsingState[]{VALUE_QUOTED}
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
        COMP_HAS_LENGTH,
        COMP_MATCHES,
        COMP_STARTS_WITH,
    };

    ParsingState(ParsingStateHandler handler) {
        this.handler = handler;
    }

    private final ParsingStateHandler handler;

    public Boolean accept(String token) {
        return this.handler.accept(token);
    }

    public void apply(ParsingContext context, String token) {
        this.handler.apply(context, token);
    }

    public ParsingState[] nextStates(ParsingContext context) {
        return this.handler.nextStates(context);
    }
}
