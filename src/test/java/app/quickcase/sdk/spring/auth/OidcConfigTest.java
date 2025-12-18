package app.quickcase.sdk.spring.auth;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OidcConfigTest {

    @Test
    @DisplayName("should return OIDC configuration with basic details when metadata and issuer are properly initialised")
    void shouldReturnOidcConfigWithBasicDetails() {
        var metadata = OidcConfig.ProviderMetadata.builder()
                                                  .jwksUri("https://oidc/jwks")
                                                  .tokenEndpoint("https://oidc/token")
                                                  .userInfoEndpoint("https://oidc/userinfo")
                                                  .build();
        var oidcConfig = OidcConfig.builder()
                                   .issuer("https://oidc")
                                   .metadata(metadata)
                                   .build();
        
        var oidcConfiguration = oidcConfig.toOidcConfiguration();

        assertThat(oidcConfiguration, equalTo(Map.of(
                "issuer", "https://oidc",
                "jwks_uri", "https://oidc/jwks",
                "token_endpoint", "https://oidc/token",
                "userinfo_endpoint", "https://oidc/userinfo",
                "subject_types_supported", List.of("public")
        )));
    }

    @Test
    @DisplayName("should throw exception when issuer is not provided")
    void shouldThrowExceptionWhenIssuerIsNull() {
        var metadata = OidcConfig.ProviderMetadata.builder()
                                                  .jwksUri("https://oidc/jwks")
                                                  .tokenEndpoint("https://oidc/token")
                                                  .userInfoEndpoint("https://oidc/userinfo")
                                                  .build();
        var oidcConfig = OidcConfig.builder()
                                   // <-- no issuer provided
                                   .metadata(metadata)
                                   .build();

        assertThrows(
                IllegalArgumentException.class,
                oidcConfig::toOidcConfiguration,
                "OIDC issuer must be provided"
        );
    }

    @Test
    @DisplayName("should throw exception when metadata is not provided")
    void shouldThrowExceptionWhenMetadataIsNull() {
        var oidcConfig = OidcConfig.builder()
                                   .issuer("https://oidc")
                                   // <-- no metadata provided
                                   .build();

        assertThrows(
                IllegalArgumentException.class,
                oidcConfig::toOidcConfiguration,
                "OIDC metadata must be provided"
        );
    }

    @Test
    @DisplayName("should throw exception when JWKS URI is missing in metadata")
    void shouldThrowExceptionWhenJwksUriIsNull() {
        var metadata = OidcConfig.ProviderMetadata.builder()
                                                  // <-- no JWKS URI provided
                                                  .tokenEndpoint("https://oidc/token")
                                                  .userInfoEndpoint("https://oidc/userinfo")
                                                  .build();
        var oidcConfig = OidcConfig.builder()
                                   .issuer("https://oidc")
                                   .metadata(metadata)
                                   .build();

        assertThrows(
                IllegalArgumentException.class,
                oidcConfig::toOidcConfiguration,
                "OIDC JWK Set URI must be provided"
        );
    }

    @Test
    @DisplayName("should throw exception when token endpoint is missing in metadata")
    void shouldThrowExceptionWhenTokenEndpointIsNull() {
        var metadata = OidcConfig.ProviderMetadata.builder()
                                                  .jwksUri("https://oidc/jwks")
                                                  // <-- no token endpoint provided
                                                  .userInfoEndpoint("https://oidc/userinfo")
                                                  .build();
        var oidcConfig = OidcConfig.builder()
                                   .issuer("https://oidc")
                                   .metadata(metadata)
                                   .build();

        assertThrows(
                IllegalArgumentException.class,
                oidcConfig::toOidcConfiguration,
                "OIDC token endpoint must be explicitly provided"
        );
    }
}