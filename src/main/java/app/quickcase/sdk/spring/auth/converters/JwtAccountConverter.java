package app.quickcase.sdk.spring.auth.converters;

import java.util.List;
import java.util.Optional;

import app.quickcase.sdk.spring.auth.OidcConfigDefault;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Given a JWT token, attempt to extract the account identifier.
 * As this is a private claim, it cannot be mandated and could be null.
 */
@Slf4j
public class JwtAccountConverter implements Converter<Jwt, String> {
    private final String accountClaim;

    public JwtAccountConverter() {
        accountClaim = OidcConfigDefault.Claims.QC_ACCOUNT;
    }

    public JwtAccountConverter(String accountClaim) {
        this.accountClaim = accountClaim;
    }

    @Override
    public String convert(@NonNull Jwt jwt) {
        return jwt.getClaimAsString(accountClaim);
    }
}
