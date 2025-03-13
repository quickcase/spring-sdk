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
import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;
import org.springframework.http.HttpMethod;

@Slf4j
public class AccessLogFilter implements Filter {
    private static final String KEY_METHOD = "requestMethod";
    private static final String KEY_URI = "requestUri";
    private static final String KEY_STATUS = "responseCode";
    private static final String KEY_DURATION = "duration";

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

            log.atLevel(logLevelStrategy.onCompleted(method, requestURI, status))
               .addKeyValue(KEY_METHOD, method)
               .addKeyValue(KEY_URI, requestURI)
               .addKeyValue(KEY_STATUS, status)
               .addKeyValue(KEY_DURATION, duration.toMillis())
               .log("Request processed ({}) in {}ms: {} {}", status, duration.toMillis(), method, requestURI);

        } catch (Exception exception) {
            final Duration duration = Duration.between(start, clock.instant());

            log.atLevel(logLevelStrategy.onException(method, requestURI, exception))
               .addKeyValue(KEY_METHOD, method)
               .addKeyValue(KEY_URI, requestURI)
               .addKeyValue(KEY_DURATION, duration.toMillis())
               .log("Request failed in {}ms: {} {} ", duration.toMillis(), method, requestURI);

            throw exception;
        }
    }

    private LoggingEventBuilder successLogBuilder (String httpMethod) {
        if (HttpMethod.OPTIONS.matches(httpMethod)) {
            return log.atDebug();
        }
        return log.atInfo();
    }
}
