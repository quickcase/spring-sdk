package app.quickcase.sdk.spring.auth;

import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

import static app.quickcase.sdk.spring.auth.OidcConfigDefault.*;
import static app.quickcase.sdk.spring.auth.OidcConfigDefault.Claims.*;

/**
 * Consolidated configuration of all properties under `quickcase.oidc` namespace.
 * All optional configuration properties have default values provided to avoid null checks.
 */
@Value
@ConfigurationProperties(prefix = "quickcase.oidc")
public class OidcConfig {
    /**
     * One of: `legacy`, `roles`
     *
     * @deprecated only `roles` should be used going forward; scheduled for removal in v2.0.0
     */
    @Deprecated(forRemoval = true)
    String authorisationStrategy;
    String jwkSetUri;
    String userInfoUri;
    String openidScope;
    Claims claims;

    @ConstructorBinding
    public OidcConfig(
            @DefaultValue(AUTHORISATION_STRATEGY) String authorisationStrategy,
            String jwkSetUri,
            String userInfoUri,
            @DefaultValue(OPENID_SCOPE) String openidScope,
            @DefaultValue Claims claims
    ) {
        this.authorisationStrategy = authorisationStrategy;
        this.jwkSetUri = jwkSetUri;
        this.userInfoUri = userInfoUri;
        this.openidScope = openidScope;
        this.claims = claims;
    }

    @Value
    public static class Claims {
        /**
         * Prefix applied to all private claims.
         */
        String prefix;

        /**
         * Names of all claims, standard and private, used by QuickCase.
         */
        ClaimNames names;

        public Claims(@DefaultValue(PREFIX) String prefix,
                      @DefaultValue ClaimNames names) {
            this.prefix = prefix;
            this.names = names;
        }
    }

    @Value
    public static class ClaimNames {
        String sub;
        String name;
        String email;
        String roles;
        String groups;
        String organisations;
        String defaultJurisdiction;
        String defaultCaseType;
        String defaultState;

        public ClaimNames(@DefaultValue(SUB) String sub,
                          @DefaultValue(NAME) String name,
                          @DefaultValue(EMAIL) String email,
                          @DefaultValue(QC_ROLES) String roles,
                          @DefaultValue(QC_GROUPS) String groups,
                          @DefaultValue(QC_ORGANISATIONS) String organisations,
                          @DefaultValue(QC_USER_DEFAULT_JURISDICTION) String defaultJurisdiction,
                          @DefaultValue(QC_USER_DEFAULT_CASE_TYPE) String defaultCaseType,
                          @DefaultValue(QC_USER_DEFAULT_STATE) String defaultState) {
            this.sub = sub;
            this.name = name;
            this.email = email;
            this.roles = roles;
            this.groups = groups;
            this.organisations = organisations;
            this.defaultJurisdiction = defaultJurisdiction;
            this.defaultCaseType = defaultCaseType;
            this.defaultState = defaultState;
        }
    }
}
