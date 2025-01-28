package app.quickcase.sdk.spring.condition;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(callSuper=false)
public class Criteria extends ConditionNode {
    private final String path;
    private final String operator;
    private final Object value;
    private final Boolean ignoreCase;
    private final Boolean negated;

    public static class CriteriaBuilder {
        private Boolean ignoreCase = false;
        private Boolean negated = false;

        public CriteriaBuilder negate() {
            this.negated = !this.negated;
            return this;
        }
    }
}
