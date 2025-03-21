package app.quickcase.sdk.spring.auth.userinfo;

import java.util.Optional;
import java.util.Set;

import app.quickcase.sdk.spring.auth.OidcException;
import app.quickcase.sdk.spring.auth.QuickcaseAuthenticationConverter;
import app.quickcase.sdk.spring.auth.QuickcaseUserAuthentication;
import app.quickcase.sdk.spring.auth.claims.ClaimsParser;
import app.quickcase.sdk.spring.auth.claims.JsonClaimsParser;
import app.quickcase.sdk.spring.auth.converters.JwtAccountConverter;
import app.quickcase.sdk.spring.auth.converters.JwtClientIdConverter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * @deprecated UserInfo parsing deprecated; scheduled for removal in v2.0.0
 */
@Deprecated(forRemoval = true)
public class UserInfoAuthenticationConverter extends QuickcaseAuthenticationConverter {
    private static final String CLAIM_SUB = "sub";

    private final UserInfoGateway userInfoGateway;

    public UserInfoAuthenticationConverter(
            JwtClientIdConverter clientIdConverter,
            JwtAccountConverter accountConverter,
            UserInfoGateway userInfoGateway,
            UserInfoExtractor userInfoExtractor,
            String openidScope
    ) {
        super(clientIdConverter, accountConverter, userInfoExtractor, openidScope);
        this.userInfoGateway = userInfoGateway;
    }

    @Override
    protected QuickcaseUserAuthentication userAuthentication(Jwt jwt, Set<String> scopes, String clientId, String account) {
        final String subject = jwt.getSubject();
        final ClaimsParser claims = new JsonClaimsParser(userInfoGateway.getClaims(jwt.getTokenValue()));

        validateSubject(subject, claims);

        final UserInfo userInfo = userInfoExtractor.extract(claims);

        return new QuickcaseUserAuthentication(jwt, authorities(scopes, userInfo.getRoles()), userInfo, clientId, account);
    }

    /**
     * Prevent token substitution attacks by validating `sub` claim.
     *
     * @param expectedSubject Subject expected by caller
     * @param claims Claims received from userInfo endpoint
     * @throws AuthenticationException When subjects cannot be compared or do not match.
     */
    private void validateSubject(String expectedSubject, ClaimsParser claims) {
        final Optional<String> actualSubject = claims.getString(CLAIM_SUB);

        if(actualSubject.isEmpty() || !expectedSubject.equals(actualSubject.get())) {
            throw new OidcException("User info subject does match expected subject");
        }
    }
}
