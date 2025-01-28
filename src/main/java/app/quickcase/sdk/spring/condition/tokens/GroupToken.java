package app.quickcase.sdk.spring.condition.tokens;

public class GroupToken extends Token {

    public GroupToken(char firstCharacter) {
        super(firstCharacter);
    }

    @Override
    public Boolean accept(char character) {
        // Group delimiters are always single characters
        return false;
    }
}
