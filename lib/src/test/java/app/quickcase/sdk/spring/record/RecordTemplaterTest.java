package app.quickcase.sdk.spring.record;

import java.time.LocalDateTime;
import java.time.Month;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

class RecordTemplaterTest {
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final String WORKSPACE = "workspace-1";
    private static final String TYPE = "type-1";
    private static final String REFERENCE = "1111222233334444";
    private static final String STATE = "in-progress";
    private static final LocalDateTime CREATED = LocalDateTime.of(2023, Month.FEBRUARY, 14, 23, 11);
    private static final LocalDateTime MODIFIED = LocalDateTime.of(2023, Month.MARCH, 11, 11, 23);
    private static final String CLASSIFICATION = "PRIVATE";
    private static final String DATA = """
            {
                "firstName": "Henry"
            }
            """;

    @Test
    @DisplayName("should render template and index paths")
    void renderAndIndex() {
        final RecordExtractor extractor = new RecordExtractor(record());
        final RecordTemplater templater = new RecordTemplater(extractor);

        final String output = templater.render("Hello {{firstName}}");

        assertThat(output, equalTo("Hello Henry"));
    }

    private Record record() {
        return new Record() {
            @Override
            public String getWorkspace() {
                return WORKSPACE;
            }

            @Override
            public String getType() {
                return TYPE;
            }

            @Override
            public String getReference() {
                return REFERENCE;
            }

            @Override
            public String getState() {
                return STATE;
            }

            @Override
            public LocalDateTime getCreated() {
                return CREATED;
            }

            @Override
            public LocalDateTime getLastModified() {
                return MODIFIED;
            }

            @Override
            public String getClassification() {
                return CLASSIFICATION;
            }

            @Override
            public ObjectNode getData() {
                try {
                    return (ObjectNode) mapper.readTree(DATA);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public ObjectNode getDataClassification() {
                return null;
            }
        };
    }
}