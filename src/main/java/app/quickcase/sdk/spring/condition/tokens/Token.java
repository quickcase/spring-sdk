package app.quickcase.sdk.spring.condition.tokens;

public abstract class Token {
    protected String value;

    Token(char firstCharacter) {
        this.value = String.valueOf(firstCharacter);
    }

    public String getValue() {
        return this.value;
    }

    public Boolean append(char character) {
        if (accept(character)) {
            this.value = this.value + character;
            return true;
        }
        return false;
    }

    public abstract Boolean accept(char character);
}
