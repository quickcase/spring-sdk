package app.quickcase.sdk.spring.condition.normalise;

import java.util.Arrays;
import java.util.stream.Stream;

import app.quickcase.sdk.spring.condition.ConditionNode;
import app.quickcase.sdk.spring.condition.Criteria;
import app.quickcase.sdk.spring.condition.Group;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static app.quickcase.sdk.spring.condition.BinaryOperator.AND;
import static app.quickcase.sdk.spring.condition.BinaryOperator.OR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class ConditionNormaliserTest {

    private static Arguments args(String name, ConditionNode[] condition, Criteria[][] expected) {
        return Arguments.of(name, condition, expected);
    }

    private static ConditionNode[] condition(ConditionNode... nodes) {
        return nodes;
    }

    private static Group group(ConditionNode... members) {
        return new Group(false, Arrays.asList(members));
    }

    private static Criteria criteria(String path) {
        return Criteria.builder().path(path).operator("EQUALS").value("value-" + path).build();
    }

    private static Criteria[][] disjunctions(Criteria[]... disjunctions) {
        return disjunctions;
    }

    private static Criteria[] conjunctions(Criteria... conjunctions) {
        return conjunctions;
    }

    private static Criteria A = criteria("A");
    private static Criteria B = criteria("B");
    private static Criteria C = criteria("C");
    private static Criteria D = criteria("D");
    private static Criteria E = criteria("E");
    private static Criteria F = criteria("F");
    private static Criteria G = criteria("G");
    private static Criteria H = criteria("H");
    private static Criteria I = criteria("I");
    private static Criteria J = criteria("J");

    @Test
    @DisplayName("should return single empty conjunction for empty condition")
    void emptyCondition() {
        final ConditionNormaliser normaliser = new ConditionNormaliser();

        assertThat(normaliser.normalise(new ConditionNode[]{}), equalTo(
            disjunctions(
                conjunctions()
            )
        ));
    }

    private static Stream<Arguments> singleLevelTestCases() {
        return Stream.of(
            args(
                "Simple condition",
                condition(A),
                disjunctions(conjunctions(A))
            ),
            args(
                "Strip redundant grouping (ie. single member)",
                condition(group(A)),
                disjunctions(conjunctions(A))
            ),
            args(
                "Strip redundant grouping recursively (ie. single member)",
                condition(group(group(group(group(A))))),
                disjunctions(conjunctions(A))
            ),
            args(
                "Disjunctions only",
                condition(A, OR, B, OR, C),
                disjunctions(
                    conjunctions(A),
                    conjunctions(B),
                    conjunctions(C)
                )
            ),
            args(
                "Conjunctions only",
                condition(A, AND, B, AND, C),
                disjunctions(
                    conjunctions(A, B, C)
                )
            ),
            args(
                "Left-to-right associativity, conjunction first",
                condition(A, AND, B, OR, C),
                disjunctions(
                    conjunctions(A, B),
                    conjunctions(C)
                )
            ),
            args(
                "Left-to-right associativity, disjunction first",
                condition(A, OR, B, AND, C),
                disjunctions(
                    conjunctions(A, C),
                    conjunctions(B, C)
                )
            ),
            args(
                "Left-to-right associativity, mixed",
                condition(A, OR, B, AND, C, OR, D, AND, E, AND, F),
                disjunctions(
                    conjunctions(A, C, E, F),
                    conjunctions(B, C, E, F),
                    conjunctions(D, E, F)
                )
            )
        );
    }

    @DisplayName("Single level (ie. no grouping)")
    @ParameterizedTest(name = "{0}")
    @MethodSource("singleLevelTestCases")
    void singleLevelConditions(String name, ConditionNode[] condition, Criteria[][] expected) {
        final ConditionNormaliser normaliser = new ConditionNormaliser();

        assertThat(normaliser.normalise(condition), equalTo(expected));
    }

    private static Stream<Arguments> twoLevelsTestCases() {
        return Stream.of(
            args(
                "Strip redundant grouping (ie. single member)",
                condition(group(A, AND, B)),
                disjunctions(conjunctions(A, B))
            ),
            args(
                "Strip redundant grouping recursively (ie. single member)",
                condition(group(group(group(group(A, AND, B))))),
                disjunctions(conjunctions(A, B))
            ),
            args(
                "Leading group",
                condition(group(A, OR, B), AND, C),
                disjunctions(
                    conjunctions(A, C),
                    conjunctions(B, C)
                )
            ),
            args(
                "Trailing group",
                condition(A, OR, group(B, AND, C)),
                disjunctions(
                    conjunctions(A),
                    conjunctions(B, C)
                )
            ),
            args(
                "Groups on both sides",
                condition(group(A, AND, B), OR, group(C, AND, D)),
                disjunctions(
                    conjunctions(A, B),
                    conjunctions(C, D)
                )
            ),
            args(
                "Left-to-right associativity in leading group",
                condition(group(A, AND, B, OR, C), OR, D),
                disjunctions(
                    conjunctions(A, B),
                    conjunctions(C),
                    conjunctions(D)
                )
            ),
            args(
                "Left-to-right associativity in trailing group",
                condition(A, AND, group(B, AND, C, OR, D)),
                disjunctions(
                    conjunctions(A, B, C),
                    conjunctions(A, D)
                )
            ),
            args(
                "Left-to-right associativity, mixed",
                condition(
                    group(A, OR, B),
                    AND,
                    group(C, OR, D),
                    AND,
                    group(E, OR, F),
                    OR,
                    group(G, AND, H)
                ),
                disjunctions(
                    conjunctions(A, C, E),
                    conjunctions(A, C, F),
                    conjunctions(A, D, E),
                    conjunctions(A, D, F),
                    conjunctions(B, C, E),
                    conjunctions(B, C, F),
                    conjunctions(B, D, E),
                    conjunctions(B, D, F),
                    conjunctions(G, H)
                )
            )
        );
    }

    @DisplayName("2 levels (ie. no nested groups)")
    @ParameterizedTest(name = "{0}")
    @MethodSource("twoLevelsTestCases")
    void twoLevelsConditions(String name, ConditionNode[] condition, Criteria[][] expected) {
        final ConditionNormaliser normaliser = new ConditionNormaliser();

        assertThat(normaliser.normalise(condition), equalTo(expected));
    }

    private static Stream<Arguments> nLevelsTestCases() {
        return Stream.of(
            args(
                "Deeply nested, disjunctions only",
                condition(
                    group(
                        group(A, OR, group(B, OR, C)),
                        OR,
                        group(D, OR, E),
                        OR,
                        group(F, OR, G, OR, H)
                    ),
                    OR,
                    I,
                    OR,
                    J
                ),
                disjunctions(
                    conjunctions(A),
                    conjunctions(B),
                    conjunctions(C),
                    conjunctions(D),
                    conjunctions(E),
                    conjunctions(F),
                    conjunctions(G),
                    conjunctions(H),
                    conjunctions(I),
                    conjunctions(J)
                )
            ),
            args(
                "Deeply nested, conjunctions only",
                condition(
                    group(
                        group(A, AND, group(B, AND, C)),
                        AND,
                        group(D, AND, E),
                        AND,
                        group(F, AND, G, AND, H)
                    ),
                    AND,
                    I,
                    AND,
                    J
                ),
                disjunctions(
                    conjunctions(A, B, C, D, E, F, G, H, I, J)
                )
            ),
            args(
                "4-level nesting, mixed OR/AND",
                condition(
                    group(
                        group(A, AND, group(B, OR, C)),
                        AND,
                        group(D, AND, E),
                        AND,
                        group(F, OR, G, OR, H)
                    ),
                    OR,
                    I,
                    OR,
                    J
                ),
                disjunctions(
                    conjunctions(A, B, D, E, F),
                    conjunctions(A, B, D, E, G),
                    conjunctions(A, B, D, E, H),
                    conjunctions(A, C, D, E, F),
                    conjunctions(A, C, D, E, G),
                    conjunctions(A, C, D, E, H),
                    conjunctions(I),
                    conjunctions(J)
                )
            ),
            args(
                "Deeply nested redundant grouping",
                condition(
                    group(group(
                        group(group(A, AND, group(group(B)))),
                        OR, C
                    ))
                ),
                disjunctions(
                    conjunctions(A, B),
                    conjunctions(C)
                )
            )
        );
    }

    @DisplayName("N levels (ie. nested groups)")
    @ParameterizedTest(name = "{0}")
    @MethodSource("nLevelsTestCases")
    void nLevelsConditions(String name, ConditionNode[] condition, Criteria[][] expected) {
        final ConditionNormaliser normaliser = new ConditionNormaliser();

        assertThat(normaliser.normalise(condition), equalTo(expected));
    }
}
