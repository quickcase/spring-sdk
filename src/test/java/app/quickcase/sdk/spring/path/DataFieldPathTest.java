package app.quickcase.sdk.spring.path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DataFieldPathTest {

    @Test
    @DisplayName("should append member to the path")
    void shouldAppendMemberToPath() {
        var path = new DataFieldPath("complexField");
        assertEquals(new DataFieldPath("complexField.member1"), path.appendMember("member1"));
    }

    @Test
    @DisplayName("should append collection item selector: identifier")
    void shouldAppendCollectionItemIdentifier() {
        var path = new DataFieldPath("collectionField");
        assertEquals(new DataFieldPath("collectionField[id:item1]"), path.appendItemSelector("item1"));
    }

    @Test
    @DisplayName("should append collection item selector: index")
    void shouldAppendCollectionItemIndex() {
        var path = new DataFieldPath("collectionField");
        assertEquals(new DataFieldPath("collectionField[42]"), path.appendItemSelector(42));
    }

    @Nested
    class AlternativePath {
        @Test
        @DisplayName("should have no alternative path by default")
        void shouldHaveNoAlternativePathByDefault() {
            var path = new DataFieldPath("collection[id:item1]");
            assertNull(path.getAlternativePath());
        }

        @Test
        @DisplayName("should capture alternative path")
        void shouldCaptureAlternativePath() {
            var path = new DataFieldPath("collection[id:item1]", "collection[0]");
            assertEquals("collection[id:item1]", path.toString());
            assertEquals("collection[0]", path.getAlternativePath());
        }

        @Test
        @DisplayName("should propagate alternative path to all descendants")
        void shouldPropagateAlternativePathToAllDescendants() {
            var path = new DataFieldPath("collection[id:item1]", "collection[0]")
                    .appendMember("value")
                    .appendMember("member1")
                    .appendItemSelector("subItem1");

            assertEquals("collection[id:item1].value.member1[id:subItem1]", path.toString());
            assertEquals("collection[0].value.member1[id:subItem1]", path.getAlternativePath());
        }

        @Test
        @DisplayName("should record alternative path for collection items")
        void shouldRecordAlternativePathForCollectionItems() {
            var path = new DataFieldPath("collection")
                    .appendItemSelector("item2", 1)
                    .appendMember("value")
                    .appendMember("member1")
                    .appendItemSelector("subItem3", 2);

            assertEquals("collection[id:item2].value.member1[id:subItem3]", path.toString());
            assertEquals("collection[1].value.member1[2]", path.getAlternativePath());
        }
    }

}