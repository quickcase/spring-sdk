package app.quickcase.sdk.spring.condition.tokens;

/**
 * Swallowed and ignored character.
 */
public record VoidToken() implements Token {
    @Override
    public String value() {
        return null;
    }
}
