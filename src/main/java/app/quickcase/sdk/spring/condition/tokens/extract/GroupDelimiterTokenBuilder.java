package app.quickcase.sdk.spring.condition.tokens.extract;

import app.quickcase.sdk.spring.condition.tokens.GroupDelimiterToken;
import app.quickcase.sdk.spring.condition.tokens.Token;

public class GroupDelimiterTokenBuilder extends TokenBuilder {
    GroupDelimiterTokenBuilder(char firstCharacter) {
        super(firstCharacter);
    }

    @Override
    public Boolean accept(char character) {
        // Group delimiters are always single characters
        return false;
    }

    @Override
    public Token build() {
        return new GroupDelimiterToken(this.value);
    }
}
