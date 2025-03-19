package app.quickcase.sdk.spring.auth;

import lombok.Getter;

@Getter
public enum SecurityClassification {
    RESTRICTED(10),
    PRIVATE(20),
    PUBLIC(30);

    private final int precedence;

    SecurityClassification(int precedence) {
        this.precedence = precedence;
    }

    public boolean highestPrecedenceThan(SecurityClassification classification) {
        return precedence > classification.getPrecedence();
    }

    public boolean lowestPrecedenceThan(SecurityClassification classification) {
        return precedence < classification.getPrecedence();
    }
}
