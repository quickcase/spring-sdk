package app.quickcase.sdk.spring.condition.validate;

import java.util.Set;

import app.quickcase.sdk.spring.condition.Condition;

public interface ConditionValidator {
    Set<String> validate(Condition condition);
}
