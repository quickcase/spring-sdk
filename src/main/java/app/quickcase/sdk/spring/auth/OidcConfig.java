package app.quickcase.sdk.spring.auth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Builder;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

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
    @Nullable
    Client client;
    @Nullable
    String issuer;
    ProviderMetadata metadata;
    String openidScope;
    /**
     * One of: `jwt-access-token`, `user-info`
     *
     * @deprecated only `jwt-access-token` should be used going forward; scheduled for removal in v2.0.0
     */
    @Deprecated(forRemoval = true)
    String mode;
    Claims claims;

    @Builder
    @ConstructorBinding
    public OidcConfig(
            @DefaultValue(AUTHORISATION_STRATEGY) String authorisationStrategy,
            Client client,
            @NonNull String issuer,
            @Nullable ProviderMetadata metadata,
            @DefaultValue(OPENID_SCOPE) String openidScope,
            @DefaultValue(MODE) String mode,
            @DefaultValue Claims claims
    ) {
        this.authorisationStrategy = authorisationStrategy;
        this.client = client;
        this.issuer = issuer;
        this.metadata = metadata;
        this.openidScope = openidScope;
        this.mode = mode;
        this.claims = claims;
    }

    public Map<String, Object> toOidcConfiguration() {
        Assert.notNull(issuer, "OIDC issuer must be provided");
        Assert.notNull(metadata, "OIDC metadata must be provided");
        Assert.notNull(metadata.jwksUri(), "OIDC JWK Set URI must be provided");
        Assert.notNull(metadata.tokenEndpoint(), "OIDC token endpoint must be explicitly provided");

        var oidcConfiguration = new HashMap<String, Object>();

        oidcConfiguration.put("issuer", issuer);
        oidcConfiguration.put("jwks_uri", metadata.jwksUri());
        oidcConfiguration.put("token_endpoint", metadata.tokenEndpoint());

        if (metadata.userInfoEndpoint() != null)
            oidcConfiguration.put("userinfo_endpoint", metadata.userInfoEndpoint());

        // Required by Spring Security
        oidcConfiguration.put("subject_types_supported", List.of("public"));

        return oidcConfiguration;
    }

    @Builder
    public record Client(
            String id,
            String secret,
            Set<String> scope
    ) {
    }

    @Builder
    public record ProviderMetadata(
            String jwksUri,
            String tokenEndpoint,
            String userInfoEndpoint
    ) {
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
        String account;
        String roles;
        String groups;
        String organisations;
        String defaultJurisdiction;
        String defaultCaseType;
        String defaultState;

        public ClaimNames(@DefaultValue(SUB) String sub,
                          @DefaultValue(NAME) String name,
                          @DefaultValue(EMAIL) String email,
                          @DefaultValue(QC_ACCOUNT) String account,
                          @DefaultValue(QC_ROLES) String roles,
                          @DefaultValue(QC_GROUPS) String groups,
                          @DefaultValue(QC_ORGANISATIONS) String organisations,
                          @DefaultValue(QC_USER_DEFAULT_JURISDICTION) String defaultJurisdiction,
                          @DefaultValue(QC_USER_DEFAULT_CASE_TYPE) String defaultCaseType,
                          @DefaultValue(QC_USER_DEFAULT_STATE) String defaultState) {
            this.sub = sub;
            this.name = name;
            this.email = email;
            this.account = account;
            this.roles = roles;
            this.groups = groups;
            this.organisations = organisations;
            this.defaultJurisdiction = defaultJurisdiction;
            this.defaultCaseType = defaultCaseType;
            this.defaultState = defaultState;
        }
    }
}
