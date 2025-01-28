package app.quickcase.sdk.spring.condition.tokens;

/**
 * Swallowed and ignored character.
 */
public class VoidToken extends Token {

    public VoidToken() {
        super((char)0);
    }

    @Override
    public Boolean accept(char character) {
        return false;
    }
}
