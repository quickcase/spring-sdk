package app.quickcase.sdk.spring.logging;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.slf4j.MDC;

/**
 * Given web apps handle requests by allocating threads from a pool and then reusing threads, we must ensure that the
 * MDC values for a given request are always cleared at the end of the request.
 */
public class MDCBoundaryFilter implements Filter {
    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
