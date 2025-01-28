package app.quickcase.sdk.spring.condition.tokens.parse;

import java.util.ArrayDeque;
import java.util.Deque;

import app.quickcase.sdk.spring.condition.ConditionNode;
import app.quickcase.sdk.spring.condition.Criteria;
import app.quickcase.sdk.spring.condition.Group;

public class ParsingContext {
    // LIFO queue of group to write to, always writing to last group in the queue
    final Deque<Group> groupStack = new ArrayDeque<>();

    // Criteria currently being built
    Criteria.CriteriaBuilder criteriaBuilder;

    Boolean negateNext = false;

    public ParsingContext() {
        // Initialize with top-level group for root
        groupStack.add(new Group(false));
    }

    public Group newGroup() {
        final Group parent = this.groupStack.getLast();
        final Group group = parent.addChildGroup(this.negateNext);
        this.groupStack.addLast(group);

        if (this.negateNext) {
            this.negateNext = false;
        }

        return group;
    }

    public Group getCurrentGroup() {
        return this.groupStack.getLast();
    }

    public void closeCurrentGroup() {
        this.groupStack.removeLast();
    }

    public Boolean insideNestedGroup() {
        // True whenever there's more than the top-level group in the stack
        return this.groupStack.size() > 1;
    }

    public Criteria.CriteriaBuilder newCriteria() {
        this.criteriaBuilder = Criteria.builder();

        if (this.negateNext) {
            // Consume and reset negation flag
            this.criteriaBuilder.negated(true);
            this.negateNext = false;
        }

        return this.criteriaBuilder;
    }

    public Criteria.CriteriaBuilder getCriteriaBuilder() {
        return this.criteriaBuilder;
    }

    public void negateNext() {
        this.negateNext = true;
    }

    public ConditionNode[] rootNodes() {
        return this.groupStack.getFirst().getMembers().toArray(ConditionNode[]::new);
    }
}
