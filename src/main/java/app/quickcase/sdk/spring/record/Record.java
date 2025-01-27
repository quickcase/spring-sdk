package app.quickcase.sdk.spring.record;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * An interface describing the core contract of a QuickCase's record.
 */
public interface Record {
    String getWorkspace();

    String getType();

    String getReference();

    String getState();

    LocalDateTime getCreated();

    LocalDateTime getLastModified();

    String getClassification();

    ObjectNode getData();

    ObjectNode getDataClassification();
}
