package app.quickcase.sdk.spring.condition.tokens.parse.state;

import app.quickcase.sdk.spring.condition.tokens.parse.ParsingContext;

interface ParsingStateHandler {

    /**
     * Does the current state accept this token?
     *
     * @param token Token being parsed
     * @return Whether the token is accepted by the current state
     */
    Boolean accept(String token);

    /**
     * In the given context, what are the possible next states the current state can transition to?
     *
     * @param context Current parsing context
     * @return Possible next states
     */
    ParsingState[] nextStates(ParsingContext context);

    /**
     * Consume and apply the current token to the parsing context.
     *
     * @param context Parsing context
     * @param token Current token
     */
    void apply(ParsingContext context, String token);
}
