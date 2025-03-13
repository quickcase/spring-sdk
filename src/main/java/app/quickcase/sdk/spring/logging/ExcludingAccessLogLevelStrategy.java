package app.quickcase.sdk.spring.logging;

import java.util.Set;

import lombok.Builder;
import lombok.Singular;
import org.slf4j.event.Level;
import org.springframework.http.HttpMethod;

/**
 * Simple strategy to "exclude" requests matching either the provided HTTP methods or URI patterns from access logs.
 * Exclusion is performed by lowering log level for the request to the specified exclusion level (TRACE by default).
 *
 * Non-matched requests will be logged at their default level: DEBUG on reception, INFO on completion and ERROR on
 * unhandled exception.
 */
@Builder
public class ExcludingAccessLogLevelStrategy implements AccessLogLevelStrategy {
    /**
     * Request HTTP methods to exclude from access logs.
     */
    @Singular
    private final Set<HttpMethod> excludedMethods;

    /**
     * Request URI patterns to exclude from access logs.
     */
    @Singular
    private final Set<String> excludedUriPatterns;

    /**
     * The level at which requests matching exclusion criteria will be logged.
     */
    @Builder.Default
    private Level exclusionLevel = Level.TRACE;

    @Override
    public Level onReceived(String method, String uri) {
        if (excluded(method, uri)) {
            return exclusionLevel;
        }

        return AccessLogLevelStrategy.super.onReceived(method, uri);
    }

    @Override
    public Level onCompleted(String method, String uri, int status) {
        if (excluded(method, uri)) {
            return exclusionLevel;
        }

        return AccessLogLevelStrategy.super.onCompleted(method, uri, status);
    }

    private boolean excluded(String method, String uri) {
        if (excludedMethods.stream().anyMatch((httpMethod) -> httpMethod.matches(method))) {
            return true;
        }

        return excludedUriPatterns.stream().anyMatch(uri::matches);
    }
}
