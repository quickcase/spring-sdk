package app.quickcase.sdk.spring.condition.tokens.parse.state;

import java.util.Arrays;
import java.util.List;

import app.quickcase.sdk.spring.condition.tokens.parse.ParsingContext;
import app.quickcase.sdk.spring.utils.ArrayUtils;

class CaseSensitiveOperatorStateHandler extends OperatorStateHandler {

    private final String targetToken;
    private final List<String> caseInsensitiveTokens;

    public CaseSensitiveOperatorStateHandler(String targetToken, String[] caseSensitiveTokens, String[] caseInsensitiveTokens, ParsingState[] nextStates) {
        super(
            // Concat case-sensitive and insensitive tokens
            ArrayUtils.concat(caseSensitiveTokens, caseInsensitiveTokens),
            nextStates
        );
        this.targetToken = targetToken;
        this.caseInsensitiveTokens = Arrays.asList(caseInsensitiveTokens);
    }

    @Override
    public void apply(ParsingContext context, String token) {
        context.getCriteriaBuilder().operator(targetToken);

        if (caseInsensitiveTokens.contains(token)) {
            context.getCriteriaBuilder().ignoreCase(true);
        }
    }
}
