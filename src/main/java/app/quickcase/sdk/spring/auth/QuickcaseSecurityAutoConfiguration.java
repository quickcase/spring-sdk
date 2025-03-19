package app.quickcase.sdk.spring.auth;

import java.net.URI;
import java.net.URISyntaxException;

import app.quickcase.sdk.spring.auth.claims.ClaimNamesProvider;
import app.quickcase.sdk.spring.auth.userinfo.UserInfoAuthenticationConverter;
import app.quickcase.sdk.spring.auth.userinfo.UserInfoExtractor;
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

    @Bean
    @ConditionalOnMissingBean(UserInfoExtractor.class)
    public UserInfoExtractor createUserInfoExtractor(ClaimNamesProvider claimNamesProvider) {
        return new UserInfoExtractor(claimNamesProvider);
    }

    @Bean
    @ConditionalOnMissingBean(UserInfoGateway.class)
    @ConditionalOnProperty(prefix = "quickcase.oidc", name = "mode", havingValue = "user-info", matchIfMissing = true)
    public UserInfoGateway createUserInfoGateway(OidcConfig oidcConfig) throws URISyntaxException {
        return new UserInfoGateway(new URI(oidcConfig.getUserInfoUri()), new RestTemplate());
    }

    @Bean
    @ConditionalOnMissingBean(QuickcaseAuthenticationConverter.class)
    @ConditionalOnProperty(prefix = "quickcase.oidc", name = "mode", havingValue = "user-info", matchIfMissing = true)
    public UserInfoAuthenticationConverter createUserInfoAuthenticationConverter(
            UserInfoGateway userInfoGateway,
            UserInfoExtractor userInfoExtractor,
            OidcConfig oidcConfig
    ) {
        return new UserInfoAuthenticationConverter(userInfoGateway, userInfoExtractor, oidcConfig.getOpenidScope());
    }

    @Bean
    @ConditionalOnMissingBean(QuickcaseAuthenticationConverter.class)
    @ConditionalOnProperty(prefix = "quickcase.oidc", name = "mode", havingValue = "jwt-access-token")
    public QuickcaseAuthenticationConverter createAccessTokenAuthenticationConverter(
            UserInfoExtractor userInfoExtractor,
            OidcConfig oidcConfig
    ) {
        return new QuickcaseAuthenticationConverter(userInfoExtractor, oidcConfig.getOpenidScope());
    }
}
