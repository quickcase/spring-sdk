package app.quickcase.sdk.spring.condition;

import app.quickcase.sdk.spring.condition.tokens.extract.TokensExtractor;
import app.quickcase.sdk.spring.condition.normalise.ConditionNormaliser;
import app.quickcase.sdk.spring.condition.tokens.parse.TokensParser;

public class ConditionParser {
    private final TokensExtractor extractor = new TokensExtractor();
    private final TokensParser parser = new TokensParser();
    private final ConditionNormaliser normaliser = new ConditionNormaliser();

    public Condition parse(String conditionString) {
        return new Condition(normaliser.normalise(parser.parse(extractor.extract(conditionString))));
    }
}
