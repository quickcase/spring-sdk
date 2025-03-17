package app.quickcase.sdk.spring.logging.logback;

import java.util.Collection;
import java.util.Map;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.encoder.EncoderBase;

import static ch.qos.logback.core.CoreConstants.NULL_STR;
import static ch.qos.logback.core.encoder.JsonEscapeUtil.jsonEscapeString;
import static java.util.stream.Collectors.toMap;

/**
 * A take on {@link ch.qos.logback.classic.encoder.JsonEncoder} simplified and with different opinions:
 * <ul>
 *     <li>Serialise `formattedMessage` as `message` with raw message and arguments dropped</li>
 *     <li>Serialise key/value pairs as object `kvp`, not list: keys should not be duplicated or they will be overridden</li>
 *     <li>Serialise stacktrace as `exception` instead of `throwable`</li>
 *     <li>Empty lists/maps are implicitly hidden instead of being serialised as `null`</li>
 * </ul>
 */
public class JsonEncoder extends EncoderBase<ILoggingEvent> {
    static int DEFAULT_SIZE = 1024;
    static int DEFAULT_SIZE_WITH_THROWABLE = DEFAULT_SIZE * 8;

    static byte[] EMPTY_BYTES = new byte[0];

    protected static final String TIMESTAMP_ATTR_NAME = "timestamp";
    protected static final String LEVEL_ATTR_NAME = "level";
    protected static final String LOGGER_ATTR_NAME = "loggerName";
    protected static final String THREAD_NAME_ATTR_NAME = "threadName";
    protected static final String MARKERS_ATTR_NAME = "markers";
    protected static final String MDC_ATTR_NAME = "mdc";
    protected static final String MESSAGE_ATTR_NAME = "message";
    protected static final String KEY_VALUE_PAIRS_ATTR_NAME = "kvp";
    protected static final String THROWABLE_ATTR_NAME = "exception";

    protected static final String CYCLIC_THROWABLE_ATTR_NAME = "cyclic";
    protected static final String CAUSE_ATTR_NAME = "cause";
    protected static final String SUPPRESSED_ATTR_NAME = "suppressed";
    protected static final String COMMON_FRAMES_COUNT_ATTR_NAME = "commonFramesCount";

    protected static final String CLASS_NAME_ATTR_NAME = "className";
    protected static final String METHOD_NAME_ATTR_NAME = "methodName";
    protected static final String FILE_NAME_ATTR_NAME = "fileName";
    protected static final String LINE_NUMBER_ATTR_NAME = "lineNumber";

    protected static final String STEP_ARRAY_NAME_ATTRIBUTE = "stepArray";

    protected static final char OPEN_OBJ = '{';
    protected static final char CLOSE_OBJ = '}';
    protected static final char OPEN_ARRAY = '[';
    protected static final char CLOSE_ARRAY = ']';

    protected static final char QUOTE = '"';
    protected static final char SP = ' ';
    protected static final String QUOTE_COL = "\":";
    protected static final char VALUE_SEPARATOR = ',';

    @Override
    public byte[] headerBytes() {
        return EMPTY_BYTES;
    }

    @Override
    public byte[] encode(ILoggingEvent event) {
        final int initialCapacity = event.getThrowableProxy() == null ? DEFAULT_SIZE : DEFAULT_SIZE_WITH_THROWABLE;
        StringBuilder sb = new StringBuilder(initialCapacity);

        sb.append(OPEN_OBJ);

        // Timestamp
        appenderMemberWithLongValue(sb, TIMESTAMP_ATTR_NAME, event.getTimeStamp());

        // Level
        sb.append(VALUE_SEPARATOR);
        String levelStr = event.getLevel() != null ? event.getLevel().levelStr : NULL_STR;
        appenderMember(sb, LEVEL_ATTR_NAME, levelStr);

        // Thread
        sb.append(VALUE_SEPARATOR);
        appenderMember(sb, THREAD_NAME_ATTR_NAME, jsonEscape(event.getThreadName()));

        // Logger
        sb.append(VALUE_SEPARATOR);
        appenderMember(sb, LOGGER_ATTR_NAME, event.getLoggerName());

        // Markers
        appendMarkers(sb, event);

        // MDC
        appendMDC(sb, event);

        // Message
        sb.append(VALUE_SEPARATOR);
        appenderMember(sb, MESSAGE_ATTR_NAME, jsonEscape(event.getFormattedMessage()));

        // Key/value pairs
        appendKeyValuePairs(sb, event);

        // Throwable
        appendException(sb, event);

        sb.append(CLOSE_OBJ);
        sb.append(CoreConstants.JSON_LINE_SEPARATOR);

        return sb.toString().getBytes(CoreConstants.UTF_8_CHARSET);
    }

    @Override
    public byte[] footerBytes() {
        return EMPTY_BYTES;
    }

    private void appenderMember(StringBuilder sb, String key, String value) {
        sb.append(QUOTE).append(key).append(QUOTE_COL).append(QUOTE).append(value).append(QUOTE);
    }

    private void appenderMemberWithIntValue(StringBuilder sb, String key, int value) {
        sb.append(QUOTE).append(key).append(QUOTE_COL).append(value);
    }

    private void appenderMemberWithLongValue(StringBuilder sb, String key, long value) {
        sb.append(QUOTE).append(key).append(QUOTE_COL).append(value);
    }

    private void appendMarkers(StringBuilder sb, ILoggingEvent event) {
        var markerList = event.getMarkerList();

        if (isEmptyCollection(markerList))
            return;

        sb.append(VALUE_SEPARATOR);
        sb.append(QUOTE).append(MARKERS_ATTR_NAME).append(QUOTE_COL).append(SP).append(OPEN_ARRAY);
        final int len = markerList.size();
        for (int i = 0; i < len; i++) {
            if (i != 0)
                sb.append(VALUE_SEPARATOR);
            sb.append(QUOTE).append(jsonEscapedToString(markerList.get(i))).append(QUOTE);

        }
        sb.append(CLOSE_ARRAY);
    }

    private void appendMDC(StringBuilder sb, ILoggingEvent event) {
        var mdc = event.getMDCPropertyMap();

        if (isEmptyMap(mdc))
            return;

        sb.append(VALUE_SEPARATOR);
        appendMap(sb, MDC_ATTR_NAME, mdc);
    }

    private void appendKeyValuePairs(StringBuilder sb, ILoggingEvent event) {
        var kvpList = event.getKeyValuePairs();

        if (isEmptyCollection(kvpList))
            return;

        sb.append(VALUE_SEPARATOR);
        appendMap(sb, KEY_VALUE_PAIRS_ATTR_NAME, kvpList.stream().collect(toMap(kvp -> kvp.key, kvp -> kvp.value)));
    }

    private void appendException(StringBuilder sb, ILoggingEvent event) {
        var itp = event.getThrowableProxy();

        if (itp == null)
            return;

        appendThrowableProxy(sb, THROWABLE_ATTR_NAME, itp, true);
    }

    private void appendThrowableProxy(StringBuilder sb, String attributeName, IThrowableProxy itp) {
        appendThrowableProxy(sb, attributeName, itp, true);
    }

    private void appendThrowableProxy(StringBuilder sb, String attributeName, IThrowableProxy itp, boolean appendValueSeparator) {

        if (appendValueSeparator)
            sb.append(VALUE_SEPARATOR);

        // in the nominal case, attributeName != null. However, attributeName will be null for suppressed
        // IThrowableProxy array, in which case no attribute name is needed
        if (attributeName != null) {
            sb.append(QUOTE).append(attributeName).append(QUOTE_COL);
            if (itp == null) {
                sb.append(NULL_STR);
                return;
            }
        }

        sb.append(OPEN_OBJ);

        appenderMember(sb, CLASS_NAME_ATTR_NAME, nullSafeStr(itp.getClassName()));

        sb.append(VALUE_SEPARATOR);
        appenderMember(sb, MESSAGE_ATTR_NAME, jsonEscape(itp.getMessage()));

        if (itp.isCyclic()) {
            sb.append(VALUE_SEPARATOR);
            appenderMember(sb, CYCLIC_THROWABLE_ATTR_NAME, jsonEscape("true"));
        }

        sb.append(VALUE_SEPARATOR);
        appendSTEPArray(sb, itp.getStackTraceElementProxyArray(), itp.getCommonFrames());

        if (itp.getCommonFrames() != 0) {
            sb.append(VALUE_SEPARATOR);
            appenderMemberWithIntValue(sb, COMMON_FRAMES_COUNT_ATTR_NAME, itp.getCommonFrames());
        }

        IThrowableProxy cause = itp.getCause();
        if (cause != null) {
            appendThrowableProxy(sb, CAUSE_ATTR_NAME, cause);
        }

        IThrowableProxy[] suppressedArray = itp.getSuppressed();
        if (suppressedArray != null && suppressedArray.length != 0) {
            sb.append(VALUE_SEPARATOR);
            sb.append(QUOTE).append(SUPPRESSED_ATTR_NAME).append(QUOTE_COL);
            sb.append(OPEN_ARRAY);

            boolean first = true;
            for (IThrowableProxy suppressedITP : suppressedArray) {
                appendThrowableProxy(sb, null, suppressedITP, !first);
                if (first)
                    first = false;
            }
            sb.append(CLOSE_ARRAY);
        }

        sb.append(CLOSE_OBJ);

    }

    private void appendSTEPArray(StringBuilder sb, StackTraceElementProxy[] stepArray, int commonFrames) {
        sb.append(QUOTE).append(STEP_ARRAY_NAME_ATTRIBUTE).append(QUOTE_COL).append(OPEN_ARRAY);

        int len = stepArray != null ? stepArray.length : 0;

        if (commonFrames >= len) {
            commonFrames = 0;
        }

        for (int i = 0; i < len - commonFrames; i++) {
            if (i != 0)
                sb.append(VALUE_SEPARATOR);

            StackTraceElementProxy step = stepArray[i];

            sb.append(OPEN_OBJ);
            StackTraceElement ste = step.getStackTraceElement();

            appenderMember(sb, CLASS_NAME_ATTR_NAME, nullSafeStr(ste.getClassName()));
            sb.append(VALUE_SEPARATOR);

            appenderMember(sb, METHOD_NAME_ATTR_NAME, nullSafeStr(ste.getMethodName()));
            sb.append(VALUE_SEPARATOR);

            appenderMember(sb, FILE_NAME_ATTR_NAME, nullSafeStr(ste.getFileName()));
            sb.append(VALUE_SEPARATOR);

            appenderMemberWithIntValue(sb, LINE_NUMBER_ATTR_NAME, ste.getLineNumber());
            sb.append(CLOSE_OBJ);

        }

        sb.append(CLOSE_ARRAY);
    }

    private void appendMap(StringBuilder sb, String attrName, Map<String, ?> map) {
        sb.append(QUOTE).append(attrName).append(QUOTE_COL).append(SP);

        if (map == null) {
            sb.append(NULL_STR);
            return;
        }

        sb.append(OPEN_OBJ);

        boolean addComma = false;
        var entries = map.entrySet();
        for (var entry : entries) {
            if (addComma) {
                sb.append(VALUE_SEPARATOR);
            }
            addComma = true;
            appenderMember(sb, jsonEscapedToString(entry.getKey()), jsonEscapedToString(entry.getValue()));
        }

        sb.append(CLOSE_OBJ);
    }

    private String jsonEscapedToString(Object o) {
        if (o == null)
            return NULL_STR;
        return jsonEscapeString(o.toString());
    }

    private String nullSafeStr(String s) {
        if (s == null)
            return NULL_STR;
        return s;
    }

    private String jsonEscape(String s) {
        if (s == null)
            return NULL_STR;
        return jsonEscapeString(s);
    }

    private boolean isEmptyMap(Map map) {
        return map == null || map.isEmpty();
    }

    private boolean isEmptyCollection(Collection collection) {
        return collection == null || collection.isEmpty();
    }
}
