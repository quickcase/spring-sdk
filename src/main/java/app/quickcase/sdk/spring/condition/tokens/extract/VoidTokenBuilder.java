package app.quickcase.sdk.spring.condition.tokens.extract;

import app.quickcase.sdk.spring.condition.tokens.Token;
import app.quickcase.sdk.spring.condition.tokens.VoidToken;

public class VoidTokenBuilder extends TokenBuilder {
    public VoidTokenBuilder() {
        super((char)0);
    }

    @Override
    public Boolean accept(char character) {
        return false;
    }

    @Override
    public Token build() {
        return new VoidToken();
    }
}
