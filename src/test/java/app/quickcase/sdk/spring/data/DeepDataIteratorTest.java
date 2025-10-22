package app.quickcase.sdk.spring.data;

import java.util.Map;
import java.util.stream.Collectors;

import app.quickcase.sdk.spring.definition.model.DataField;
import app.quickcase.sdk.spring.definition.model.RecordType;
import app.quickcase.sdk.spring.definition.model.Schema;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

class DeepDataIteratorTest {

    private static final JsonNodeFactory JSON = JsonNodeFactory.instance;

    private static final RecordType TYPE = new RecordType(new Schema(Map.of(
            "field1", DataField.builder().id("field1").name("Field 1").type("Text").classification("PUBLIC").build(),
            "field2", DataField.builder().id("field2").name("Field 2").type("Number").classification("PUBLIC").build(),
            "field3", DataField.builder().id("field3").name("Field 3").type("Text").classification("PUBLIC").build(),
            "complex1", DataField.builder().id("complex1").name("Complex 1").type("Complex").classification("PUBLIC")
                                 .member("member1", DataField.builder().id("member1").name("Member 1").type("Text").classification("PUBLIC").build())
                                 .member("member2", DataField.builder().id("member2").name("Member 2").type("Text").classification("PUBLIC").build())
                                 .member("member3", DataField.builder().id("member3").name("Member 3").type("Text").classification("PUBLIC").build())
                                 .build(),
            "collection1", DataField.builder().id("collection1").name("Collection 1").type("Collection").classification("PUBLIC")
                                    .content(DataField.Content.builder().type("Complex")
                                                              .member("member1", DataField.builder().id("member1").name("Member 1").type("Text").classification("PUBLIC").build())
                                                              .member("member2", DataField.builder().id("member2").name("Member 2").type("Text").classification("PUBLIC").build())
                                                              .member("member3", DataField.builder().id("member3").name("Member 3").type("Text").classification("PUBLIC").build())
                                                              .build())
                                    .build(),
            "collection2", DataField.builder().id("collection2").name("Collection 2").type("Collection").classification("PUBLIC")
                                    .content(DataField.Content.builder().type("Text").build())
                                    .build()
    )));

    @Test
    @DisplayName("should iterate top-level data fields")
    void shouldIterateTopLevelDataFields() {
        var data = JSON.objectNode()
                       .put("field1", "value1")
                       .put("field2", "42");

        var iterator = new DeepDataIterator(TYPE, data);
        var entries = iterator.stream().collect(Collectors.toSet());

        assertThat(entries, containsInAnyOrder(
                new DataIterator.Entry("field1", TYPE.schema().fields().get("field1"), JSON.textNode("value1")),
                new DataIterator.Entry("field2", TYPE.schema().fields().get("field2"), JSON.textNode("42"))
        ));
    }

    @Test
    @DisplayName("should iterate data fields with no corresponding definition")
    void shouldIterateDataFieldsWithNoDefinition() {
        var data = JSON.objectNode()
                       .put("field1", "value1")
                       .put("fieldUnknown", "value42");

        var iterator = new DeepDataIterator(TYPE, data);
        var entries = iterator.stream().collect(Collectors.toSet());

        assertThat(entries, containsInAnyOrder(
                new DataIterator.Entry("field1", TYPE.schema().fields().get("field1"), JSON.textNode("value1")),
                new DataIterator.Entry("fieldUnknown", JSON.textNode("value42")) // <-- no definition
        ));
    }

    @Test
    @DisplayName("should recursively iterate nested complex members")
    void shouldRecursivelyIterateNestedComplexMembers() {
        var data = JSON.objectNode()
                       .put("field1", "value1");
        data.set("complex1", JSON.objectNode()
                                 .put("member1", "value1")
                                 .put("member3", "value3"));
        data.put("field2", "value2");

        var iterator = new DeepDataIterator(TYPE, data);
        var entries = iterator.stream().collect(Collectors.toSet());

        assertThat(entries, containsInAnyOrder(
                new DataIterator.Entry("field1", TYPE.schema().fields().get("field1"), JSON.textNode("value1")),
                new DataIterator.Entry("complex1", TYPE.schema().fields().get("complex1"), data.get("complex1")),
                new DataIterator.Entry("complex1.member1", TYPE.schema().fields().get("complex1").members().get("member1"), JSON.textNode("value1")),
                new DataIterator.Entry("complex1.member3", TYPE.schema().fields().get("complex1").members().get("member3"), JSON.textNode("value3")),
                new DataIterator.Entry("field2", TYPE.schema().fields().get("field2"), JSON.textNode("value2"))
        ));
    }

    @Test
    @DisplayName("should recursively iterate simple collection")
    void shouldRecursivelyIterateSimpleCollection() {
        var data = JSON.objectNode()
                       .put("field1", "value1");
        var item1 = JSON.objectNode()
                        .put("id", "item1")
                        .put("value", "item1 value");
        var item2 = JSON.objectNode()
                        .put("id", "item2")
                        .put("value", "item2 value");
        data.set("collection2", JSON.arrayNode().add(item1).add(item2));
        data.put("field2", "value2");

        var iterator = new DeepDataIterator(TYPE, data);
        var entries = iterator.stream().collect(Collectors.toSet());

        // Fake field definition generated by DefinitionExtractor for collection values
        var collectionItemField = DataField.builder()
                                           .id("collection2[].value")
                                           .name("Collection 2")
                                           .type("Text")
                                           .classification("PUBLIC")
                                           .build();

        assertThat(entries, containsInAnyOrder(
                new DataIterator.Entry("field1", TYPE.schema().fields().get("field1"), JSON.textNode("value1")),
                new DataIterator.Entry("collection2", TYPE.schema().fields().get("collection2"), data.get("collection2")),
                new DataIterator.Entry("collection2[id:item1].value", collectionItemField, JSON.textNode("item1 value")),
                new DataIterator.Entry("collection2[id:item2].value", collectionItemField, JSON.textNode("item2 value")),
                new DataIterator.Entry("field2", TYPE.schema().fields().get("field2"), JSON.textNode("value2"))
        ));
    }

    @Test
    @DisplayName("should recursively iterate nested collection members")
    void shouldRecursivelyIterateNestedCollectionMembers() {
        var data = JSON.objectNode()
                       .put("field1", "value1");
        var item1 = JSON.objectNode().put("id", "item1");
        item1.set("value", JSON.objectNode()
                               .put("member1", "value1")
                               .put("member3", "value3")
        );
        var item2 = JSON.objectNode().put("id", "item2");
        item2.set("value", JSON.objectNode()
                               .put("member2", "value2")
        );
        data.set("collection1", JSON.arrayNode().add(item1).add(item2));
        data.put("field2", "value2");

        var iterator = new DeepDataIterator(TYPE, data);
        var entries = iterator.stream().collect(Collectors.toSet());

        // Fake field definition generated by DefinitionExtractor for collection values
        var collectionItemField = DataField.builder()
                                           .id("collection1[].value")
                                           .name("Collection 1")
                                           .type(DataField.TYPE_COMPLEX)
                                           .classification("PUBLIC")
                                           .members(TYPE.schema().fields().get("collection1").content().members())
                                           .build();

        assertThat(entries, containsInAnyOrder(
                new DataIterator.Entry("field1", TYPE.schema().fields().get("field1"), JSON.textNode("value1")),
                new DataIterator.Entry("collection1", TYPE.schema().fields().get("collection1"), data.get("collection1")),
                new DataIterator.Entry("collection1[id:item1].value", collectionItemField, item1.get("value")),
                new DataIterator.Entry("collection1[id:item1].value.member1", TYPE.schema().fields().get("collection1").content().members().get("member1"), JSON.textNode("value1")),
                new DataIterator.Entry("collection1[id:item1].value.member3", TYPE.schema().fields().get("collection1").content().members().get("member3"), JSON.textNode("value3")),
                new DataIterator.Entry("collection1[id:item2].value", collectionItemField, item2.get("value")),
                new DataIterator.Entry("collection1[id:item2].value.member2", TYPE.schema().fields().get("collection1").content().members().get("member2"), JSON.textNode("value2")),
                new DataIterator.Entry("field2", TYPE.schema().fields().get("field2"), JSON.textNode("value2"))
        ));
    }

    @Test
    @DisplayName("should recursively iterate collection items without IDs")
    void shouldRecursivelyIterateCollectionItemsWithoutIDs() {
        var data = JSON.objectNode()
                       .put("field1", "value1");
        var item1 = JSON.objectNode() // ID undefined
                        .put("value", "item1 value");
        var item2 = JSON.objectNode()
                        .put("id", "item2") // ID defined, mix-n-match
                        .put("value", "item2 value");
        var item3 = JSON.objectNode()
                        .put("value", "item3 value");
        item3.set("id", JSON.nullNode()); // ID explicitly null

        data.set("collection2", JSON.arrayNode().add(item1).add(item2).add(item3));
        data.put("field2", "value2");

        var iterator = new DeepDataIterator(TYPE, data);
        var entries = iterator.stream().collect(Collectors.toSet());

        // Fake field definition generated by DefinitionExtractor for collection values
        var collectionItemField = DataField.builder()
                                           .id("collection2[].value")
                                           .name("Collection 2")
                                           .type("Text")
                                           .classification("PUBLIC")
                                           .build();

        assertThat(entries, containsInAnyOrder(
                new DataIterator.Entry("field1", TYPE.schema().fields().get("field1"), JSON.textNode("value1")),
                new DataIterator.Entry("collection2", TYPE.schema().fields().get("collection2"), data.get("collection2")),
                new DataIterator.Entry("collection2[0].value", collectionItemField, JSON.textNode("item1 value")),
                new DataIterator.Entry("collection2[id:item2].value", collectionItemField, JSON.textNode("item2 value")),
                new DataIterator.Entry("collection2[2].value", collectionItemField, JSON.textNode("item3 value")),
                new DataIterator.Entry("field2", TYPE.schema().fields().get("field2"), JSON.textNode("value2"))
        ));

        // Check correct recording of alternative path for items with ID
        var collectionItemEntry = entries.stream().filter(e -> e.path().toString().equals("collection2[id:item2].value")).findFirst().orElseThrow();
        assertThat(collectionItemEntry.path().getAlternativePath(), equalTo("collection2[1].value"));
    }

}
