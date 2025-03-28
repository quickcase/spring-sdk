package app.quickcase.sdk.spring.auth.userinfo;

import app.quickcase.sdk.spring.auth.OidcException;
import app.quickcase.sdk.spring.auth.converters.AbstractAuthenticationConverter;
import app.quickcase.sdk.spring.auth.converters.JsonUserInfoConverter;
import app.quickcase.sdk.spring.auth.converters.JwtClientIdConverter;
import app.quickcase.sdk.spring.auth.converters.JwtClientInfoConverter;
import app.quickcase.sdk.spring.auth.converters.JwtScopesConverter;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * @deprecated UserInfo parsing deprecated; scheduled for removal in v2.0.0
 */
@Deprecated(forRemoval = true)
public class UserInfoAuthenticationConverter extends AbstractAuthenticationConverter {
    private final UserInfoGateway userInfoGateway;
    private final JsonUserInfoConverter userInfoConverter;

    public UserInfoAuthenticationConverter(
            JwtClientIdConverter clientIdConverter,
            JwtScopesConverter scopesConverter,
            JwtClientInfoConverter clientInfoConverter,
            String openidScope,
            UserInfoGateway userInfoGateway,
            JsonUserInfoConverter userInfoConverter
    ) {
        super(clientIdConverter, scopesConverter, clientInfoConverter, openidScope);
        this.userInfoGateway = userInfoGateway;
        this.userInfoConverter = userInfoConverter;
    }

    @Override
    protected UserInfo convertUserInfo(@NonNull Jwt jwt) {
        var subject = jwt.getSubject();
        var claims = userInfoGateway.getClaims(jwt.getTokenValue());

        var userInfo = userInfoConverter.convert(claims);

        validateSubject(subject, userInfo);

        return userInfo;
    }

    /**
     * Prevent token substitution attacks by validating `sub` claim.
     *
     * @param expectedSubject Subject expected by caller
     * @param userInfo User info claims received from userInfo endpoint
     * @throws OidcException When subjects cannot be compared or do not match.
     */
    private void validateSubject(String expectedSubject, UserInfo userInfo) {
        var actualSubject = userInfo.getSubject();

        if(actualSubject.isEmpty() || !expectedSubject.equals(actualSubject)) {
            throw new OidcException("User info subject does not match expected subject");
        }
    }
}
