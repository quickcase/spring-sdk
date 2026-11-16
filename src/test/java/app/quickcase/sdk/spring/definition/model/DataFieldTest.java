package app.quickcase.sdk.spring.definition.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataFieldTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Nested
    class DataFieldOption {

        @Test
        @DisplayName("should default list item to active for existing fields")
        void shouldDefaultListItemsToActiveForExistingFields() {
            assertEquals(DataField.Option.Status.ACTIVE, new DataField.Option("code", "Label").status());
            assertEquals(DataField.Option.Status.ACTIVE,
                    DataField.Option.builder().code("code").label("Label").build().status());
        }

        @Test
        @DisplayName("should default list item to active when reading older schemas")
        void shouldDefaultListItemsToActiveWhenReadingOlderSchemas() {
            var option = mapper.readValue("{\"code\":\"code\",\"label\":\"Label\"}", DataField.Option.class);
            assertEquals(DataField.Option.Status.ACTIVE, option.status());
        }
    }
}
