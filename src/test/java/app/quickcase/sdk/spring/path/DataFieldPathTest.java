package app.quickcase.sdk.spring.path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

}