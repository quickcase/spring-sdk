package app.quickcase.sdk.spring.auth;

import lombok.Getter;

@Getter
public enum AccessLevel {
    ORGANISATION(10),
    GROUP(20),
    INDIVIDUAL(30);

    private final int precedence;

    AccessLevel(int precedence) {
        this.precedence = precedence;
    }

    public boolean highestPrecedenceThan(AccessLevel accessLevel) {
        return precedence > accessLevel.getPrecedence();
    }

    public boolean lowestPrecedenceThan(AccessLevel accessLevel) {
        return precedence < accessLevel.getPrecedence();
    }
}
