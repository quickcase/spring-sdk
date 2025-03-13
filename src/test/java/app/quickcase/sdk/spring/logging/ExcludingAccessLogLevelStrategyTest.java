package app.quickcase.sdk.spring.logging;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import org.springframework.http.HttpMethod;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ExcludingAccessLogLevelStrategyTest {

    @Nested
    class OnReceived {
        @Test
        @DisplayName("Given an excluded method onReceived should return exclusion level")
        public void testOnReceivedWithExcludedMethodReturnsExclusionLevel() {
            ExcludingAccessLogLevelStrategy strategy = ExcludingAccessLogLevelStrategy.builder()
                                                                                      .excludedMethods(Set.of(HttpMethod.GET))
                                                                                      .build();

            Level result = strategy.onReceived(HttpMethod.GET.name(), "/test");

            assertThat(result, is(Level.TRACE));
        }

        @Test
        @DisplayName("Given a not excluded method onReceived should return default level")
        public void testOnReceivedWithoutExcludedMethodReturnsDelegatedLevel() {
            ExcludingAccessLogLevelStrategy strategy = ExcludingAccessLogLevelStrategy.builder()
                                                                                      .excludedMethods(Set.of(HttpMethod.POST))
                                                                                      .build();

            Level result = strategy.onReceived(HttpMethod.GET.name(), "/test");

            assertThat(result, is(Level.DEBUG));
        }

        @Test
        @DisplayName("Given an excluded URI onReceived should return exclusion level")
        public void testOnReceivedWithExcludedUriReturnsExclusionLevel() {
            ExcludingAccessLogLevelStrategy strategy = ExcludingAccessLogLevelStrategy.builder()
                                                                                      .excludedUriPatterns(Set.of("/test.*"))
                                                                                      .build();

            Level result = strategy.onReceived(HttpMethod.GET.name(), "/test");

            assertThat(result, is(Level.TRACE));
        }

        @Test
        @DisplayName("Given a not excluded URI onReceived should return default level")
        public void testOnReceivedWithoutExcludedUriReturnsDefaultLevel() {
            ExcludingAccessLogLevelStrategy strategy = ExcludingAccessLogLevelStrategy.builder()
                                                                                      .excludedUriPatterns(Set.of("/abc.*"))
                                                                                      .build();

            Level result = strategy.onReceived(HttpMethod.GET.name(), "/test");

            assertThat(result, is(Level.DEBUG));
        }
    }

    @Nested
    class OnCompleted {
        @Test
        @DisplayName("Given an excluded method onCompleted should return exclusion level")
        public void testOnCompletedWithExcludedMethodReturnsExclusionLevel() {
            ExcludingAccessLogLevelStrategy strategy = ExcludingAccessLogLevelStrategy.builder()
                                                                                      .excludedMethods(Set.of(HttpMethod.GET))
                                                                                      .build();

            Level result = strategy.onCompleted(HttpMethod.GET.name(), "/test", 200);

            assertThat(result, is(Level.TRACE));
        }

        @Test
        @DisplayName("Given a not excluded method onCompleted should return default Level")
        public void testOnCompletedWithoutExcludedMethodReturnsDelegatedLevel() {
            ExcludingAccessLogLevelStrategy strategy = ExcludingAccessLogLevelStrategy.builder()
                                                                                      .excludedMethods(Set.of(HttpMethod.POST))
                                                                                      .build();

            Level result = strategy.onCompleted(HttpMethod.GET.name(), "/test", 200);

            assertThat(result, is(Level.INFO));
        }

        @Test
        @DisplayName("Given an excluded URI onCompleted should return exclusion Level")
        public void testOnCompletedWithExcludedUriReturnsExclusionLevel() {
            ExcludingAccessLogLevelStrategy strategy = ExcludingAccessLogLevelStrategy.builder()
                                                                                      .excludedUriPatterns(Set.of("/test.*"))
                                                                                      .build();

            Level result = strategy.onCompleted(HttpMethod.GET.name(), "/test", 200);

            assertThat(result, is(Level.TRACE));
        }

        @Test
        @DisplayName("Given a not excluded URI onCompleted should return default Level")
        public void testOnCompletedWithoutExcludedUriReturnsDefaultLevel() {
            ExcludingAccessLogLevelStrategy strategy = ExcludingAccessLogLevelStrategy.builder()
                                                                                      .excludedUriPatterns(Set.of("/abc.*"))
                                                                                      .build();

            Level result = strategy.onCompleted(HttpMethod.GET.name(), "/test", 200);

            assertThat(result, is(Level.INFO));
        }
    }
}
