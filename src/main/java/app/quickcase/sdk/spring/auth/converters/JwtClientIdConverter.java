package app.quickcase.sdk.spring.auth.converters;

import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Given a JWT token, attempt to extract the OAuth2 client ID from the claims `client_id` and `azp` in order of precedence.
 * As the OAuth2/OpenID RFCs do not mandate either claim, a null value can be returned.
 */
@Slf4j
public class JwtClientIdConverter implements Converter<Jwt, String> {
    private static final List<String> LOOKUP_CLAIMS = List.of("client_id", "azp");

    @Override
    public String convert(@NonNull Jwt jwt) {
        var claimName = getClaimName(jwt);

        if (claimName.isPresent()) {
            log.debug("Extracting client ID from claim {}", claimName.get());
            return jwt.getClaimAsString(claimName.get());
        }

        return null;
    }

    private Optional<String> getClaimName(Jwt jwt) {
        return LOOKUP_CLAIMS.stream().filter(claimName -> jwt.getClaim(claimName) != null).findFirst();
    }
}
