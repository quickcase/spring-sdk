package app.quickcase.sdk.spring.condition.tokens;

public class OperatorToken extends Token {

    public OperatorToken(char firstCharacter) {
        super(firstCharacter);
    }

    @Override
    public Boolean accept(char character) {
        return TokenCharacter.isOperatorSymbol(character);
    }
}
