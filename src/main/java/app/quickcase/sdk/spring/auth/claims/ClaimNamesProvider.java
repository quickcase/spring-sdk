package app.quickcase.sdk.spring.auth.claims;

import app.quickcase.sdk.spring.auth.OidcConfig;

/**
 * Dynamic provider of names for OIDC claims retrieved from access token or /userinfo endpoint.
 */
public class ClaimNamesProvider {
    private final String prefix;
    private final String sub;
    private final String name;
    private final String email;
    private final String roles;
    private final String groups;
    private final String organisations;
    private final String defaultJurisdiction;
    private final String defaultCaseType;
    private final String defaultState;

    public ClaimNamesProvider(OidcConfig.Claims claimsConfig) {
        this.prefix = claimsConfig.getPrefix();

        final OidcConfig.ClaimNames names = claimsConfig.getNames();
        this.sub = names.getSub();
        this.name = names.getName();
        this.email = names.getEmail();
        this.roles = names.getRoles();
        this.groups = names.getGroups();
        this.organisations = names.getOrganisations();
        this.defaultJurisdiction = names.getDefaultJurisdiction();
        this.defaultCaseType = names.getDefaultCaseType();
        this.defaultState = names.getDefaultState();
    }

    public String sub() {
        return sub;
    }

    public String name() {
        return name;
    }

    public String email() {
        return email;
    }

    public String roles() {
        return prefix + roles;
    }

    public String groups() {
        return prefix + groups;
    }

    /**
     * @deprecated Organisations deprecated in favour of role-driven authorisation; scheduled for removal in v2.0.0
     */
    @Deprecated(forRemoval = true)
    public String organisations() {
        return prefix + organisations;
    }

    public String defaultJurisdiction() {
        return prefix + defaultJurisdiction;
    }

    public String defaultCaseType() {
        return prefix + defaultCaseType;
    }

    public String defaultState() {
        return prefix + defaultState;
    }
}
