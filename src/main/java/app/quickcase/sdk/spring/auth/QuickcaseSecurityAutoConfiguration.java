package app.quickcase.sdk.spring.auth;

import java.net.URI;
import java.net.URISyntaxException;

import app.quickcase.sdk.spring.auth.claims.ClaimNamesProvider;
import app.quickcase.sdk.spring.auth.converters.*;
import app.quickcase.sdk.spring.auth.userinfo.UserInfoAuthenticationConverter;
import app.quickcase.sdk.spring.auth.userinfo.UserInfoGateway;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for Spring applications to auto-configure Spring Security to work with a QuickCase-compliant OIDC
 * providers.
 * This configuration relies on property `quickcase.oidc.jwk-set-uri` and optionally `quickcase.oidc.user-info-uri`
 * being defined as properties in the Spring application.
 */
@AutoConfiguration
@EnableConfigurationProperties(OidcConfig.class)
public class QuickcaseSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ClaimNamesProvider.class)
    public ClaimNamesProvider createClaimNamesProvider(OidcConfig oidcConfig) {
        return new ClaimNamesProvider(oidcConfig.getClaims());
    }

    /**
     * @deprecated UserInfo parsing deprecated; scheduled for removal in v2.0.0
     */
    @Deprecated(forRemoval = true)
    @Bean
    @ConditionalOnMissingBean(UserInfoGateway.class)
    @ConditionalOnProperty(prefix = "quickcase.oidc", name = "mode", havingValue = "user-info", matchIfMissing = true)
    public UserInfoGateway createUserInfoGateway(OidcConfig oidcConfig) throws URISyntaxException {
        return new UserInfoGateway(new URI(oidcConfig.getUserInfoUri()), new RestTemplate());
    }

    @Bean
    @ConditionalOnMissingBean(JwtClientIdConverter.class)
    public JwtClientIdConverter jwtClientIdConverter() {
        return new JwtClientIdConverter();
    }

    @Bean
    @ConditionalOnMissingBean(JwtAccountConverter.class)
    public JwtAccountConverter jwtAccountConverter(ClaimNamesProvider claimNames) {
        return new JwtAccountConverter(claimNames.account());
    }

    @Bean
    @ConditionalOnMissingBean(JwtScopesConverter.class)
    public JwtScopesConverter jwtScopesConverter() {
        return new JwtScopesConverter();
    }

    @Bean
    @ConditionalOnMissingBean(JwtRolesConverter.class)
    public JwtRolesConverter jwtRolesConverter(ClaimNamesProvider claimNames) {
        return new JwtRolesConverter(claimNames.roles());
    }

    @Bean
    @ConditionalOnMissingBean(JwtGroupsConverter.class)
    public JwtGroupsConverter jwtGroupsConverter(ClaimNamesProvider claimNames) {
        return new JwtGroupsConverter(claimNames.groups());
    }

    @Bean
    @ConditionalOnMissingBean(JwtUserInfoConverter.class)
    public JwtUserInfoConverter jwtUserInfoConverter(
            ClaimNamesProvider claimNames,
            JwtAccountConverter accountConverter,
            JwtRolesConverter rolesConverter,
            JwtGroupsConverter groupsConverter
    ) {
        return new JwtUserInfoConverter(claimNames, accountConverter, rolesConverter, groupsConverter);
    }

    @Bean
    @ConditionalOnMissingBean(JwtClientInfoConverter.class)
    public JwtClientInfoConverter jwtClientInfoConverter(
            ClaimNamesProvider claimNames,
            JwtAccountConverter accountConverter,
            JwtScopesConverter scopesConverter,
            JwtRolesConverter rolesConverter,
            JwtGroupsConverter groupsConverter
    ) {
        return new JwtClientInfoConverter(claimNames, accountConverter, scopesConverter, rolesConverter, groupsConverter);
    }

    /**
     * @deprecated UserInfo parsing deprecated; scheduled for removal in v2.0.0
     */
    @Deprecated(forRemoval = true)
    @Bean
    @ConditionalOnMissingBean(JsonUserInfoConverter.class)
    public JsonUserInfoConverter jsonUserInfoConverter(ClaimNamesProvider claimNames) {
        return new JsonUserInfoConverter(claimNames);
    }

    /**
     * @deprecated UserInfo parsing deprecated; scheduled for removal in v2.0.0
     */
    @Deprecated(forRemoval = true)
    @Bean
    @ConditionalOnMissingBean(AbstractAuthenticationConverter.class)
    @ConditionalOnProperty(prefix = "quickcase.oidc", name = "mode", havingValue = "user-info", matchIfMissing = true)
    public UserInfoAuthenticationConverter createUserInfoAuthenticationConverter(
            JwtClientIdConverter clientIdConverter,
            JwtScopesConverter scopesConverter,
            JwtClientInfoConverter clientInfoConverter,
            OidcConfig oidcConfig,
            UserInfoGateway userInfoGateway,
            JsonUserInfoConverter userInfoConverter
    ) {
        return new UserInfoAuthenticationConverter(
                clientIdConverter,
                scopesConverter,
                clientInfoConverter,
                oidcConfig.getOpenidScope(),
                userInfoGateway,
                userInfoConverter
        );
    }

    @Bean
    @ConditionalOnMissingBean(AbstractAuthenticationConverter.class)
    @ConditionalOnProperty(prefix = "quickcase.oidc", name = "mode", havingValue = "jwt-access-token")
    public JwtAuthenticationConverter createAccessTokenAuthenticationConverter(
            JwtClientIdConverter clientIdConverter,
            JwtScopesConverter scopesConverter,
            JwtUserInfoConverter userInfoConverter,
            JwtClientInfoConverter clientInfoConverter,
            OidcConfig oidcConfig
    ) {
        return new JwtAuthenticationConverter(
                clientIdConverter,
                scopesConverter,
                userInfoConverter,
                clientInfoConverter,
                oidcConfig.getOpenidScope()
        );
    }

    @Bean
    public QuickcaseOAuth2ResourceServerCustomizer oauth2ResourceServerCustomizer(
            OidcConfig oidcConfig,
            AbstractAuthenticationConverter authenticationConverter
    ) {
        return new QuickcaseOAuth2ResourceServerCustomizer(oidcConfig, authenticationConverter);
    }
}
