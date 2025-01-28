package app.quickcase.sdk.spring.condition.tokens;

public class QuotedStringToken extends Token {

    public QuotedStringToken(char firstCharacter) {
        super(firstCharacter);
    }

    @Override
    public Boolean accept(char character) {
        final Character firstCharacter = this.value.charAt(0);
        final Character lastCharacter = this.value.charAt(this.value.length() - 1);

        // Quoted string is closed when the first and last characters are distinct quote delimiter characters
        final Boolean quotedStringClosed = this.value.length() > 1 && firstCharacter.equals(lastCharacter);

        return !quotedStringClosed;
    }
}
