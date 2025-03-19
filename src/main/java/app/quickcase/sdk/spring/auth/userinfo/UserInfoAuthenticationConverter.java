package app.quickcase.sdk.spring.auth.userinfo;

import java.util.Optional;
import java.util.Set;

import app.quickcase.sdk.spring.auth.OidcException;
import app.quickcase.sdk.spring.auth.QuickcaseAuthenticationConverter;
import app.quickcase.sdk.spring.auth.QuickcaseUserAuthentication;
import app.quickcase.sdk.spring.auth.claims.ClaimsParser;
import app.quickcase.sdk.spring.auth.claims.JsonClaimsParser;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;

public class UserInfoAuthenticationConverter extends QuickcaseAuthenticationConverter {
    private static final String CLAIM_SUB = "sub";

    private final UserInfoGateway userInfoGateway;

    public UserInfoAuthenticationConverter(UserInfoGateway userInfoGateway, UserInfoExtractor userInfoExtractor, String openidScope) {
        super(userInfoExtractor, openidScope);
        this.userInfoGateway = userInfoGateway;
    }

    @Override
    protected QuickcaseUserAuthentication userAuthentication(Jwt jwt, Set<String> scopes) {
        final String subject = jwt.getSubject();
        final ClaimsParser claims = new JsonClaimsParser(userInfoGateway.getClaims(jwt.getTokenValue()));

        validateSubject(subject, claims);

        final UserInfo userInfo = userInfoExtractor.extract(claims);

        return new QuickcaseUserAuthentication(jwt, authorities(scopes, userInfo.getRoles()), userInfo);
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
