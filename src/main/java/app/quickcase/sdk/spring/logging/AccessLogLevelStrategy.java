package app.quickcase.sdk.spring.logging;

import org.slf4j.event.Level;

public interface AccessLogLevelStrategy {
    default Level onReceived(String method, String uri) {
        return Level.DEBUG;
    }
    default Level onCompleted(String method, String uri, int status) {
        return Level.INFO;
    }
    default Level onException(String method, String uri, Exception exception) {
        return Level.ERROR;
    }
}
