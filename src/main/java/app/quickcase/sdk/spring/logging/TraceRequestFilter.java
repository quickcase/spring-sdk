package app.quickcase.sdk.spring.logging;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;

/**
 * Generate a unique trace ID per request and assign it to MDC to allow grouping of logs per request.
 * If the request itself bears a custom trace ID header, this is preserved as parent trace ID.
 */
public class TraceRequestFilter implements Filter {
    public static final String X_TRACE_ID = "x-trace-id";

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        final Optional<String> traceParentId = Optional.ofNullable(httpRequest.getHeader(X_TRACE_ID));

        MDC.put("traceId", UUID.randomUUID().toString());
        traceParentId.ifPresent(id -> MDC.put("traceParentId", id));

        chain.doFilter(request, response);
    }
}
