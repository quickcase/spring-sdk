package app.quickcase.sdk.spring.auth;

public interface OidcConfigDefault {
    String AUTHORISATION_STRATEGY = "roles";
    String MODE = "user-info";
    String NAMESPACE = "app.quickcase.claims/";
    String OPENID_SCOPE = "openid";
    String PREFIX = "";

    interface Claims {
        // Standard claims
        String SUB = "sub";
        String NAME = "name";
        String EMAIL = "email";
        // Private claims
        String QC_ACCOUNT = NAMESPACE + "account";
        String QC_ROLES = NAMESPACE + "roles";
        String QC_GROUPS = NAMESPACE + "groups";
        String QC_ORGANISATIONS = NAMESPACE + "organisations";
        String QC_USER_DEFAULT_JURISDICTION = NAMESPACE + "default_jurisdiction";
        String QC_USER_DEFAULT_CASE_TYPE = NAMESPACE + "default_case_type";
        String QC_USER_DEFAULT_STATE = NAMESPACE + "default_state";
    }
}
