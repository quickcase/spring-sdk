package app.quickcase.sdk.spring.logging;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.spi.LoggingEventBuilder;
import org.springframework.web.servlet.HandlerMapping;

@Slf4j
public class AccessLogFilter implements Filter {
    private static final String KEY_METHOD = "method";
    private static final String KEY_URI = "uri";
    private static final String KEY_STATUS = "status";
    private static final String KEY_DURATION = "duration";
    private static final String KEY_MATCH = "match";

    private final static Clock clock = Clock.systemDefaultZone();
    private final AccessLogLevelStrategy logLevelStrategy;

    public AccessLogFilter() {
        this.logLevelStrategy = new DefaultAccessLogLevelStrategy();
    }

    public AccessLogFilter(AccessLogLevelStrategy logLevelStrategy) {
        this.logLevelStrategy = logLevelStrategy;
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        final HttpServletResponse httpResponse = (HttpServletResponse) response;

        final String method = httpRequest.getMethod();
        final String requestURI = httpRequest.getRequestURI();

        log.atLevel(logLevelStrategy.onReceived(method, requestURI))
           .addKeyValue(KEY_METHOD, method)
           .addKeyValue(KEY_URI, requestURI)
           .log("Request received: {} {}", method, requestURI);

        final Instant start = clock.instant();

        try {
            chain.doFilter(request, response);

            final Duration duration = Duration.between(start, clock.instant());
            final int status = httpResponse.getStatus();

            var completedLogBuilder = log.atLevel(logLevelStrategy.onCompleted(method, requestURI, status))
                                         .addKeyValue(KEY_METHOD, method)
                                         .addKeyValue(KEY_URI, requestURI)
                                         .addKeyValue(KEY_STATUS, status)
                                         .addKeyValue(KEY_DURATION, duration.toMillis());

            addPatternMatch(httpRequest, completedLogBuilder);

            completedLogBuilder.log("Request processed ({}) in {}ms: {} {}", status, duration.toMillis(), method, requestURI);

        } catch (Exception exception) {
            final Duration duration = Duration.between(start, clock.instant());

            var errorLogBuilder = log.atLevel(logLevelStrategy.onException(method, requestURI, exception))
                                     .addKeyValue(KEY_METHOD, method)
                                     .addKeyValue(KEY_URI, requestURI)
                                     .addKeyValue(KEY_DURATION, duration.toMillis());

            addPatternMatch(httpRequest, errorLogBuilder);

            errorLogBuilder.log("Request failed in {}ms: {} {} ", duration.toMillis(), method, requestURI);

            throw exception;
        }
    }

    /**
     * Extract and populate the matching URI pattern, if available, in the access log KVP.
     * The matrix of variable for that matching pattern is ignored as it would duplicate the MDC in most cases.
     *
     * @param httpRequest Request from which to extract the matched pattern
     * @param logBuilder Logging event builder to which the pattern KVP should be added
     */
    private void addPatternMatch(HttpServletRequest httpRequest, LoggingEventBuilder logBuilder) {
        var pattern = (String) httpRequest.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);

        if (pattern != null) {
            logBuilder.addKeyValue(KEY_MATCH, pattern);
        }
    }
}
