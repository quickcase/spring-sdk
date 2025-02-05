package app.quickcase.sdk.spring.logging;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

class AccessLogFilterTest {

    @Test
    @DisplayName("should execute filter chain")
    void shouldExecuteFilterChain() throws ServletException, IOException {
        var filter = new AccessLogFilter();

        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("should rethrow exception")
    void shouldRethrowException() throws ServletException, IOException {
        var filter = new AccessLogFilter();

        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var chain = mock(FilterChain.class);

        var exception = new RuntimeException("Some exception");

        doThrow(exception).when(chain).doFilter(request, response);

        var actualException = Assertions.assertThrows(
                RuntimeException.class,
                () -> filter.doFilter(request, response, chain)
        );
        assertThat(actualException, is(exception));
    }

}