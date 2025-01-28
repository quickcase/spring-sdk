package app.quickcase.sdk.spring.condition.tokens.extract;

import app.quickcase.sdk.spring.condition.tokens.QuotedStringToken;
import app.quickcase.sdk.spring.condition.tokens.Token;

public class QuotedStringTokenBuilder extends TokenBuilder {
    QuotedStringTokenBuilder(char firstCharacter) {
        super(firstCharacter);
    }

    @Override
    public Boolean accept(char character) {
        var firstCharacter = this.value.charAt(0);
        var lastCharacter = this.value.charAt(this.value.length() - 1);

        // Quoted string is closed when the first and last characters are distinct quote delimiter characters
        var quotedStringClosed = this.value.length() > 1 && firstCharacter == lastCharacter;

        return !quotedStringClosed;
    }

    @Override
    public Token build() {
        return new QuotedStringToken(this.value.substring(1, this.value.length() - 1));
    }
}
