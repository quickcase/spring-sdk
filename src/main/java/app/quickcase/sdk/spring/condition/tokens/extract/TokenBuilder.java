package app.quickcase.sdk.spring.condition.tokens.extract;

import app.quickcase.sdk.spring.condition.tokens.Token;

public abstract class TokenBuilder {
    protected String value;

    TokenBuilder(char firstCharacter) {
        this.value = String.valueOf(firstCharacter);
    }

    public Boolean append(char character) {
        if (accept(character)) {
            this.value = this.value + character;
            return true;
        }
        return false;
    }

    public abstract Boolean accept(char character);

    public abstract Token build();
}
