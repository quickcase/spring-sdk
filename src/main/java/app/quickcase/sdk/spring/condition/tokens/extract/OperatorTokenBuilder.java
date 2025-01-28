package app.quickcase.sdk.spring.condition.tokens.extract;

import app.quickcase.sdk.spring.condition.tokens.OperatorToken;
import app.quickcase.sdk.spring.condition.tokens.Token;

public class OperatorTokenBuilder extends TokenBuilder {
    OperatorTokenBuilder(char firstCharacter) {
        super(firstCharacter);
    }

    @Override
    public Boolean accept(char character) {
        return TokenCharacter.isOperatorSymbol(character);
    }

    @Override
    public Token build() {
        return new OperatorToken(this.value);
    }
}
