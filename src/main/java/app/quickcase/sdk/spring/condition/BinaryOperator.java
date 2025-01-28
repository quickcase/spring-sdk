package app.quickcase.sdk.spring.condition;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BinaryOperator extends ConditionNode {
    public static final BinaryOperator AND = new BinaryOperator("AND");
    public static final BinaryOperator OR = new BinaryOperator("OR");

    public static BinaryOperator valueOf(String identifier) {
        if (AND.identifier.equals(identifier)) {
            return AND;
        }
        if (OR.identifier.equals(identifier)) {
            return OR;
        }

        throw new IllegalArgumentException("No boolean operator for identifier: " + identifier);
    }

    private final String identifier;

    public ConditionNode negate() {
        return AND.equals(this) ? OR : AND;
    }
}
