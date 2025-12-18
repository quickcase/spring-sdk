package app.quickcase.sdk.spring.auth;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * Configuration for Spring applications to auto-configure Spring Security to work with a QuickCase-compliant OIDC
 * providers.
 * This configuration relies on properties `quickcase.oidc.*` being defined as properties in the Spring application.
 */
@AutoConfiguration
@EnableConfigurationProperties(OidcConfig.class)
@Import({
        QuickcaseOAuth2ClientConfiguration.class,
        QuickcaseOAuth2ResourceServerConfiguration.class,
})
public class QuickcaseSecurityAutoConfiguration {
}
