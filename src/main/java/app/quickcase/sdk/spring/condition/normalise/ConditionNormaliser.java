package app.quickcase.sdk.spring.condition.normalise;

import java.util.Arrays;
import java.util.stream.Stream;

import app.quickcase.sdk.spring.condition.BinaryOperator;
import app.quickcase.sdk.spring.condition.ConditionNode;
import app.quickcase.sdk.spring.condition.Criteria;
import app.quickcase.sdk.spring.condition.Group;

import static app.quickcase.sdk.spring.condition.BinaryOperator.OR;

/**
 * Third step of condition parsing: any conditional expression composed of criteria, groups, `AND` and `OR` can be
 * normalised into a 2-dimensional structure of criteria where the first dimension represents disjunctions (`OR`), the
 * second dimension represents conjunctions (`AND`) and the items are all criteria.
 * <br>
 * Evaluation is then greatly simplified as disjunctions and conjunctions are dictated by the structure, not tokens, and
 * the concept of group has been completely removed.
 */
public class ConditionNormaliser {

    public Criteria[][] normalise(ConditionNode[] condition) {
        if (condition.length == 0) {
            return new Criteria[][] {{}};
        }

        Criteria[][] disjunctions = normalise(condition[0]);

        for(int i = 1; i < condition.length; i = i + 2) {
            if (!(condition[i] instanceof BinaryOperator operator)) {
                throw new IllegalArgumentException(
                    String.format("Expected operator at condition index %d, received: %s", i, condition[i])
                );
            }

            final Criteria[][] right = normalise(condition[i + 1]);

            disjunctions = OR.equals(operator) ? disjunction(disjunctions, right) : conjunction(disjunctions, right);
        }

        return disjunctions;
    }

    private Criteria[][] normalise(ConditionNode node) {
        if (node instanceof Criteria) {
            return normalise((Criteria) node);
        }

        if (node instanceof Group) {
            return normalise((Group) node);
        }

        throw new IllegalArgumentException("Only group and criteria nodes can be normalised, received: " + node);
    }

    private Criteria[][] normalise(Group group) {
        return normalise(group.getMembers().toArray(ConditionNode[]::new));
    }

    private Criteria[][] normalise(Criteria criteria) {
        return new Criteria[][] {{criteria}};
    }

    private Criteria[][] disjunction(Criteria[][] left, Criteria[][] right) {
        return Stream.concat(
            Arrays.stream(left),
            Arrays.stream(right)
        ).toArray(Criteria[][]::new);
    }

    private Criteria[][] conjunction(Criteria[][] left, Criteria[][] right) {
        return Arrays.stream(left)
                     .flatMap((leftConjunctions) -> Arrays.stream(right)
                                                          .map((rightConjunctions) -> Stream.concat(
                                                              Arrays.stream(leftConjunctions),
                                                              Arrays.stream(rightConjunctions)
                                                          ).toArray(Criteria[]::new))
                     ).toArray(Criteria[][]::new);
    }
}
