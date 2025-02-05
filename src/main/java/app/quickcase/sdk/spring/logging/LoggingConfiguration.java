package app.quickcase.sdk.spring.logging;

import jakarta.servlet.Filter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Spring configuration class to import in web app using Spring's @Import annotation in order to enable MDC support,
 * access logs and request tracing.
 */
@Configuration
public class LoggingConfiguration {
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public Filter mdcBoundaryFilter() {
        return new MDCBoundaryFilter();
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    public Filter traceRequestFilter() {
        return new TraceRequestFilter();
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 2)
    public Filter accessLogFilter() {
        return new AccessLogFilter();
    }
}
