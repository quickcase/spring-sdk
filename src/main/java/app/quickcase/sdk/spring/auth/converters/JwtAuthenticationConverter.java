package app.quickcase.sdk.spring.auth.converters;

import app.quickcase.sdk.spring.auth.userinfo.UserInfo;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Extract all QuickCase user claims from the access token.
 */
public class JwtAuthenticationConverter extends AbstractAuthenticationConverter {
    protected final JwtUserInfoConverter userInfoConverter;

    public JwtAuthenticationConverter(
            JwtClientIdConverter clientIdConverter,
            JwtScopesConverter scopesConverter,
            JwtUserInfoConverter userInfoConverter,
            JwtClientInfoConverter clientInfoConverter,
            String openidScope
    ) {
        super(clientIdConverter, scopesConverter, clientInfoConverter, openidScope);
        this.userInfoConverter = userInfoConverter;
    }

    @Override
    protected UserInfo convertUserInfo(@NonNull Jwt jwt) {
        return userInfoConverter.convert(jwt);
    }
}
