package app.quickcase.sdk.spring.auth;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.util.Assert;

import static org.springframework.security.oauth2.client.registration.ClientRegistrations.fromIssuerLocation;
import static org.springframework.security.oauth2.client.registration.ClientRegistrations.fromOidcConfiguration;

@Configuration
@ConditionalOnProperty(prefix = "quickcase.oidc.client", name = "id")
public class QuickcaseOAuth2ClientConfiguration {
    @Bean
    public ClientRegistration defaultOAuth2Client(
            OidcConfig oidcConfig
    ) {
        var issuer = oidcConfig.getIssuer();
        var client = oidcConfig.getClient();

        Assert.notNull(client, "OIDC client must be defined");
        Assert.notNull(client.id(), "OIDC client ID must be defined");
        Assert.notNull(client.secret(), "OIDC client secret must be defined");

        Assert.notNull(issuer, "OIDC issuer is required for client registration");

        var clientRegistrationBuilder = oidcConfig.getMetadata() != null
                ? fromOidcConfiguration(oidcConfig.toOidcConfiguration())
                : fromIssuerLocation(issuer);

        return clientRegistrationBuilder
                .registrationId("default")
                .clientName("S2S Client")
                .clientId(client.id())
                .clientSecret(client.secret())
                .scope(client.scope())
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(ClientRegistrationRepository.class)
    public ClientRegistrationRepository clientRegistrationRepository(List<ClientRegistration> registrations) {
        return new InMemoryClientRegistrationRepository(registrations);
    }

}
