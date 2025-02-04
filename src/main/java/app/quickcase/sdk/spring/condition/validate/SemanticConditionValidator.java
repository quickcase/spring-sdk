package app.quickcase.sdk.spring.condition.validate;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import app.quickcase.sdk.spring.condition.Condition;
import app.quickcase.sdk.spring.condition.Criteria;
import app.quickcase.sdk.spring.definition.DefinitionExtractor;
import app.quickcase.sdk.spring.path.FieldPath;

public class SemanticConditionValidator implements ConditionValidator {

    private final DefinitionExtractor fieldExtractor;

    public SemanticConditionValidator(DefinitionExtractor fieldExtractor) {
        this.fieldExtractor = fieldExtractor;
    }

    @Override
    public Set<String> validate(Condition condition) {
        var allPaths = Arrays.stream(condition.disjunctions())
                             .flatMap(Arrays::stream)
                             .map(Criteria::getPath)
                             .collect(Collectors.toSet());

        return allPaths.stream()
                       .flatMap(this::validate)
                       .collect(Collectors.toSet());
    }

    private Stream<String> validate(String path) {
        try {
            var fieldPath = FieldPath.of(path);

            if (fieldExtractor.extractField(fieldPath).isEmpty()) {
                return Stream.of("Path `" + path + "` does not exist");
            }

            return Stream.empty();
        } catch (IllegalArgumentException e) {
            return Stream.of("Path `" + path + "` is not valid");
        }
    }
}
