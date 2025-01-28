package app.quickcase.sdk.spring.condition.tokens;

public class TextToken extends Token {

    public TextToken(char firstCharacter) {
        super(firstCharacter);
    }

    @Override
    public Boolean accept(char character) {
        return TokenCharacter.isText(character);
    }
}
