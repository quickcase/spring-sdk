package app.quickcase.sdk.spring.auth;

import app.quickcase.sdk.spring.auth.converters.AbstractAuthenticationConverter;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Customizer to use in Spring application to configure Spring's {@link SecurityFilterChain}.
 * <br>
 * For example:
 *
 * <pre>
 * {@code
 *     @Bean
 *     public SecurityFilterChain filterChain(
 *         HttpSecurity http,
 *         QuickcaseOAuth2ResourceServerCustomizer quickcaseOAuth2ResourceServer
 *     ) throws Exception {
 *         return http
 *             //... other configuration
 *             .oauth2ResourceServer(quickcaseOAuth2ResourceServer)
 *             .build();
 *     }
 * }
 * </pre>
 */
public class QuickcaseOAuth2ResourceServerCustomizer implements Customizer<OAuth2ResourceServerConfigurer<HttpSecurity>> {
    private final OidcConfig oidcConfig;
    private final AbstractAuthenticationConverter authenticationConverter;

    public QuickcaseOAuth2ResourceServerCustomizer(OidcConfig oidcConfig, AbstractAuthenticationConverter authenticationConverter) {
        this.oidcConfig = oidcConfig;
        this.authenticationConverter = authenticationConverter;
    }

    @Override
    public void customize(OAuth2ResourceServerConfigurer<HttpSecurity> oauth2ResourceServer) {
        oauth2ResourceServer.jwt(jwt -> {
            // Add support for JWT type `at+jwt` used by some OIDC providers
            var jwsTypeVerifier = new DefaultJOSEObjectTypeVerifier<>(JOSEObjectType.JWT, new JOSEObjectType("at+jwt"));

            jwt.decoder(
                    NimbusJwtDecoder.withJwkSetUri(oidcConfig.getJwkSetUri())
                                    .jwtProcessorCustomizer(processor -> {
                                        processor.setJWSTypeVerifier(jwsTypeVerifier);
                                    })
                                    .build()
            );
            jwt.jwtAuthenticationConverter(authenticationConverter);
        });
    }
}
