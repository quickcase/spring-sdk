package app.quickcase.sdk.spring.condition;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class Group extends ConditionNode {
    private final Boolean negated;
    private final List<ConditionNode> members;

    public Group(Boolean negated) {
        this(negated, new ArrayList<>());
    }

    public Group(Boolean negated, List<ConditionNode> members) {
        this.negated = negated;
        this.members = members;
    }

    public void addCriteria(Criteria.CriteriaBuilder criteriaBuilder) {
        if (this.negated) {
            criteriaBuilder.negate();
        }

        this.members.add(criteriaBuilder.build());
    }

    public void addBinaryOperator(BinaryOperator binaryOperator) {
        this.members.add(this.negated ? binaryOperator.negate(): binaryOperator);
    }

    public Group addChildGroup(Boolean negated) {
        final Group childGroup = new Group(this.negated ^ negated);

        this.members.add(childGroup);

        return childGroup;
    }
}
