package app.quickcase.sdk.spring.logging.logback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.event.KeyValuePair;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

class JsonEncoderTest {
    private static final JsonNodeFactory JSON = new JsonNodeFactory();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    final JsonEncoder encoder = new JsonEncoder();

    @Test
    @DisplayName("should serialise simple log event")
    void shouldSerialiseSimpleLogEvent() {
        var event = loggingEvent().withLevel(Level.WARN).build();

        var log = MAPPER.readTree(encoder.encode(event));

        assertAll(
                () -> assertThat(log.get("loggerName").asString(), is("a.b.c.TestLogger")),
                () -> assertThat(log.get("level").asString(), is("WARN")),
                () -> assertThat(log.get("message").asString(), is("Simple log event"))
        );
    }

    @Test
    @DisplayName("should serialise formatted message")
    void withFormattedMessage() {
        var event = loggingEvent().withMessage("Formatted message {}: {}")
                                  .withArguments("hello", "world")
                                  .build();

        var log = MAPPER.readTree(encoder.encode(event));

        assertThat(log.get("message").asString(), is("Formatted message hello: world"));
    }

    @Test
    @DisplayName("should serialise markers")
    void withMarkers() {
        var event = loggingEvent().withMarker("MARKER_1")
                                  .withMarker("MARKER_2")
                                  .build();

        var log = MAPPER.readTree(encoder.encode(event));
        var logMarkers = log.get("markers");

        assertAll(
                () -> assertThat(logMarkers.size(), equalTo(2)),
                () -> assertThat(logMarkers.get(0).asString(), is("MARKER_1")),
                () -> assertThat(logMarkers.get(1).asString(), is("MARKER_2"))
        );
    }

    @Test
    @DisplayName("should serialise MDC")
    void withMdc() {
        var event = loggingEvent().withMdcEntry("mdcKey1", "value1")
                                  .withMdcEntry("mdcKey2", "value2")
                                  .build();

        var log = MAPPER.readTree(encoder.encode(event));
        var logMdc = log.get("mdc");

        assertAll(
                () -> assertThat(logMdc.size(), equalTo(2)),
                () -> assertThat(logMdc.get("mdcKey1").asString(), is("value1")),
                () -> assertThat(logMdc.get("mdcKey2").asString(), is("value2"))
        );
    }

    @Test
    @DisplayName("should serialise key/value pairs")
    void withKvp() {
        var event = loggingEvent().withKvp("key1", "value1")
                                  .withKvp("key2", "value2")
                                  .build();

        var log = MAPPER.readTree(encoder.encode(event));
        var logKvp = log.get("kvp");

        assertAll(
                () -> assertThat(logKvp.size(), equalTo(2)),
                () -> assertThat(logKvp.get("key1").asString(), is("value1")),
                () -> assertThat(logKvp.get("key2").asString(), is("value2"))
        );
    }

    @Test
    @DisplayName("should serialise exception")
    void withException() {
        var event = loggingEvent().withException(new Throwable("Some exception"))
                                  .build();

        var log = MAPPER.readTree(encoder.encode(event));
        var logException = log.get("exception");

        assertThat(logException.get("message"), equalTo(JSON.stringNode("Some exception")));
    }

    LoggingEventBuilder loggingEvent() {
        return new LoggingEventBuilder();
    }

    static class LoggingEventBuilder {
        private Level level = Level.INFO;
        private String loggerName = "a.b.c.TestLogger";
        private List<Marker> markers = null;
        private Map<String, String> mdc = null;
        private String message = "Simple log event";
        private Object[] arguments = null;
        private List<KeyValuePair> kvp = null;
        private Throwable exception = null;

        public LoggingEventBuilder withLevel(Level level) {
            this.level = level;
            return this;
        }

        public LoggingEventBuilder withLoggerName(String loggerName) {
            this.loggerName = loggerName;
            return this;
        }

        public LoggingEventBuilder withMarker(String marker) {
            if (markers == null) {
                markers = new ArrayList<>();
            }

            markers.add(MarkerFactory.getMarker(marker));

            return this;
        }

        public LoggingEventBuilder withMdcEntry(String key, String value) {
            if (mdc == null) {
                mdc = new HashMap<>();
            }

            mdc.put(key, value);

            return this;
        }

        public LoggingEventBuilder withMessage(String message) {
            this.message = message;
            return this;
        }

        public LoggingEventBuilder withArguments(Object... args) {
            this.arguments = args;
            return this;
        }

        public LoggingEventBuilder withKvp(String key, String value) {
            if (kvp == null) {
                kvp = new ArrayList<>();
            }

            kvp.add(new KeyValuePair(key, value));

            return this;
        }

        public LoggingEventBuilder withException(Throwable exception) {
            this.exception = exception;
            return this;
        }

        public LoggingEvent build() {
            var event = new LoggingEvent();
            event.setLevel(level);
            event.setLoggerName(loggerName);
            if (markers != null) {
                markers.forEach(event::addMarker);
            }
            event.setMDCPropertyMap(mdc != null ? mdc : Map.of());
            event.setMessage(message);
            event.setArgumentArray(arguments);
            event.setKeyValuePairs(kvp);
            if (exception != null) {
                event.setThrowableProxy(new ThrowableProxy(exception));
            }
            return event;
        }
    }

}
