package app.quickcase.sdk.spring.condition.tokens.extract;

import app.quickcase.sdk.spring.condition.tokens.NumberToken;
import app.quickcase.sdk.spring.condition.tokens.Token;

public class NumberTokenBuilder extends TokenBuilder {
    NumberTokenBuilder(char firstCharacter) {
        super(firstCharacter);
    }

    @Override
    public Boolean accept(char character) {
        return TokenCharacter.isDigit(character);
    }

    @Override
    public Token build() {
        return new NumberToken(this.value);
    }
}
