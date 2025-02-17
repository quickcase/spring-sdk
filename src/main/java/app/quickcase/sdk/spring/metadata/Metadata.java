package app.quickcase.sdk.spring.metadata;

import lombok.NonNull;

public enum Metadata {
    WORKSPACE("[workspace]"),
    TYPE("[type]"),
    ID("[id]"),
    TITLE("[title]"),
    STATE("[state]"),
    CLASSIFICATION("[classification]"),
    CREATED_AT("[createdAt]"),
    LAST_MODIFIED_AT("[lastModifiedAt]"),
    // CaseLink metadata
    SOURCE_FIELD_PATH("[sourceFieldPath]");

    private final String path;

    Metadata(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public static Metadata fromPath(@NonNull String path) {
        if (path.length() < 2) {
            throw new IllegalArgumentException("Invalid metadata path: " + path);
        }

        var name = path.substring(1, path.length() - 1).toLowerCase();
        return switch (name) {
            case "workspace", "organisation", "jurisdiction" -> WORKSPACE;
            case "type", "case_type" -> TYPE;
            case "id", "reference", "case_reference" -> ID;
            case "title" -> TITLE;
            case "state" -> STATE;
            case "classification", "security_classification" -> CLASSIFICATION;
            case "createdat", "created", "created_date" -> CREATED_AT;
            case "lastmodifiedat", "modified", "last_modified", "last_modified_date" -> LAST_MODIFIED_AT;
            case "sourcefieldpath" -> SOURCE_FIELD_PATH;
            default -> throw new IllegalArgumentException("Invalid metadata path: " + path);
        };
    }
}
