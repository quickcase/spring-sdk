package app.quickcase.sdk.spring.definition;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Optional;

import app.quickcase.sdk.spring.definition.model.DataField;
import app.quickcase.sdk.spring.definition.model.Field;
import app.quickcase.sdk.spring.definition.model.MetadataField;
import app.quickcase.sdk.spring.definition.model.RecordType;
import app.quickcase.sdk.spring.path.DataFieldPath;
import app.quickcase.sdk.spring.path.FieldPath;
import app.quickcase.sdk.spring.path.MetadataFieldPath;

public class DefinitionExtractor {

    private final RecordType type;

    public DefinitionExtractor(RecordType type) {
        this.type = type;
    }

    public Optional<? extends Field> extractField(FieldPath path) {
        return switch (path) {
            case MetadataFieldPath metadataFieldPath -> Optional.of(extractField(metadataFieldPath));
            case DataFieldPath dataFieldPath -> extractField(dataFieldPath);
            default -> throw new IllegalArgumentException("Unsupported field path: " + path);
        };
    }

    public MetadataField extractField(MetadataFieldPath path) {
        return switch (path.getMetadata()) {
            case WORKSPACE -> MetadataField.builder()
                                           .id(path.toString())
                                           .name("Workspace")
                                           .label("Workspace")
                                           .build();
            case TYPE -> MetadataField.builder()
                                      .id(path.toString())
                                      .name("Type")
                                      .label("Record type")
                                      .build();
            case ID -> MetadataField.builder()
                                    .id(path.toString())
                                    .name("ID")
                                    .label("Record ID")
                                    .build();
            case TITLE -> MetadataField.builder()
                                       .id(path.toString())
                                       .name("Title")
                                       .label("Record title")
                                       .build();
            case STATE -> MetadataField.builder()
                                       .id(path.toString())
                                       .name("State")
                                       .label("Record state")
                                       .build();
            case CREATED_AT -> MetadataField.builder()
                                            .id(path.toString())
                                            .name("Created at")
                                            .label("Date and time of creation")
                                            .build();
            case LAST_MODIFIED_AT -> MetadataField.builder()
                                            .id(path.toString())
                                            .name("Last modified at")
                                            .label("Date and time of last modification")
                                            .build();
            case CLASSIFICATION -> MetadataField.builder()
                                                .id(path.toString())
                                                .name("Classification")
                                                .label("Default record classification")
                                                .build();
            // CaseLink metadata
            case SOURCE_FIELD_PATH -> MetadataField.builder()
                                                   .id(path.toString())
                                                   .name("Link source field")
                                                   .label("Path of the field from which the link originated")
                                                   .build();
        };
    }

    public Optional<DataField> extractField(DataFieldPath path) {
        return extractDataField(type.schema().fields(), new ArrayDeque<>(path.elements()));
    }

    private Optional<DataField> extractDataField(Map<String, DataField> fields, ArrayDeque<DataFieldPath.Element> pathElements) {
        var element = pathElements.removeFirst();
        var field = fields.get(element.getIdentifier());

        if (field == null && element.isCollectionItem()) {
            return extractDataFieldCollectionItem(fields, element.getCollectionIdentifier(), pathElements);
        }

        if (field == null) {
            return Optional.empty();
        }

        if (!pathElements.isEmpty()) {
            if (field.members() == null) {
                return Optional.empty();
            }

            return extractDataField(field.members(), pathElements);
        }

        return Optional.of(field);
    }

    private Optional<DataField> extractDataFieldCollectionItem(Map<String, DataField> fields, String collectionId, ArrayDeque<DataFieldPath.Element> elements) {
        var field = fields.get(collectionId);

        if (field == null) {
            return Optional.empty();
        }

        if (!elements.isEmpty()) {
            var itemFieldBuilder = DataField.builder()
                                     .id(field.id() + "[].value")
                                     .name(field.name())
                                     .label(field.label())
                                     .type(field.content().type())
                                     .acl(field.acl())
                                     .classification(field.classification());
            if (field.content().members() != null) {
                itemFieldBuilder.members(field.content().members());
            }

            if (field.content().options() != null) {
                itemFieldBuilder.options(field.content().options());
            }

            return extractDataField(Map.of("value", itemFieldBuilder.build()), elements);
        }

        return Optional.of(field);
    }
}
