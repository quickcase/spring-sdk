package app.quickcase.sdk.spring.logging;

import jakarta.servlet.Filter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Spring configuration class to import in web app using Spring's @Import annotation in order to enable MDC support,
 * access logs and request tracing.
 */
@Configuration
public class LoggingAutoConfiguration {
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ConditionalOnMissingBean(MDCBoundaryFilter.class)
    public Filter mdcBoundaryFilter() {
        return new MDCBoundaryFilter();
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    @ConditionalOnMissingBean(TraceRequestFilter.class)
    public Filter traceRequestFilter() {
        return new TraceRequestFilter();
    }

    @Bean
    @ConditionalOnMissingBean(AccessLogLevelStrategy.class)
    public AccessLogLevelStrategy defaultAccessLogLevelStrategy() {
        return new DefaultAccessLogLevelStrategy();
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 2)
    @ConditionalOnMissingBean(AccessLogFilter.class)
    public Filter accessLogFilter(AccessLogLevelStrategy accessLogLevelStrategy) {
        return new AccessLogFilter(accessLogLevelStrategy);
    }
}
