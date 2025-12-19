package app.quickcase.sdk.spring.auth;

import java.util.Optional;

import app.quickcase.sdk.spring.auth.converters.AbstractAuthenticationConverter;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.oauth2.jwt.JwtValidators;
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
    // Add support for JWT type `at+jwt` used by some OIDC providers
    public static final DefaultJOSEObjectTypeVerifier<SecurityContext> JWS_TYPE_VERIFIER = new DefaultJOSEObjectTypeVerifier<>(JOSEObjectType.JWT, new JOSEObjectType("at+jwt"));

    private final OidcConfig oidcConfig;
    private final AbstractAuthenticationConverter authenticationConverter;

    public QuickcaseOAuth2ResourceServerCustomizer(OidcConfig oidcConfig, AbstractAuthenticationConverter authenticationConverter) {
        this.oidcConfig = oidcConfig;
        this.authenticationConverter = authenticationConverter;
    }

    @Override
    public void customize(OAuth2ResourceServerConfigurer<HttpSecurity> oauth2ResourceServer) {
        oauth2ResourceServer.jwt(jwt -> {
            var issuer = Optional.ofNullable(oidcConfig.getIssuer());
            var metadata = Optional.ofNullable(oidcConfig.getMetadata());

            var decoderBuilder = metadata.flatMap(m -> Optional.ofNullable(m.jwksUri()))
                                         .map(NimbusJwtDecoder::withJwkSetUri)
                                         .or(() -> issuer.map(NimbusJwtDecoder::withIssuerLocation))
                                         .orElseThrow(() -> new IllegalStateException("Either JWK set URI or issuer must be provided"));

            var decoder = decoderBuilder
                                   .jwtProcessorCustomizer(processor -> {
                                       processor.setJWSTypeVerifier(JWS_TYPE_VERIFIER);
                                   })
                                   .build();

            // Enforce issuer validation whenever possible
            issuer.map(JwtValidators::createDefaultWithIssuer)
                  .ifPresent(decoder::setJwtValidator);

            jwt.decoder(decoder);
            jwt.jwtAuthenticationConverter(authenticationConverter);
        });
    }
}
