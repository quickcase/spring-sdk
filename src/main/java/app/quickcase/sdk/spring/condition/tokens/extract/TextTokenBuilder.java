package app.quickcase.sdk.spring.condition.tokens.extract;

import app.quickcase.sdk.spring.condition.tokens.TextToken;
import app.quickcase.sdk.spring.condition.tokens.Token;

public class TextTokenBuilder extends TokenBuilder {
    TextTokenBuilder(char firstCharacter) {
        super(firstCharacter);
    }

    @Override
    public Boolean accept(char character) {
        return TokenCharacter.isText(character);
    }

    @Override
    public Token build() {
        return new TextToken(this.value);
    }
}
