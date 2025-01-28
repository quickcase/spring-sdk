package app.quickcase.sdk.spring.condition;

import java.util.Arrays;
import java.util.Objects;

public record Condition(Criteria[][] disjunctions) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Condition condition = (Condition) o;
        return Objects.deepEquals(disjunctions, condition.disjunctions);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(disjunctions);
    }
}
