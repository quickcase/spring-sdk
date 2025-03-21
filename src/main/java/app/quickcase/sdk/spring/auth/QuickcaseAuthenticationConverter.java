package app.quickcase.sdk.spring.auth;

import java.util.Set;
import java.util.stream.Stream;

import app.quickcase.sdk.spring.auth.claims.ClaimsParser;
import app.quickcase.sdk.spring.auth.claims.JwtClaimsParser;
import app.quickcase.sdk.spring.auth.converters.JwtAccountConverter;
import app.quickcase.sdk.spring.auth.converters.JwtClientIdConverter;
import app.quickcase.sdk.spring.auth.userinfo.UserInfo;
import app.quickcase.sdk.spring.auth.userinfo.UserInfoExtractor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import static java.util.stream.Collectors.toSet;

/**
 * Extract all QuickCase user claims from the access token.
 */
public class QuickcaseAuthenticationConverter implements Converter<Jwt, QuickcaseAuthentication> {
    private static final String SCOPE_DELIMITER = " ";

    private final JwtClientIdConverter clientIdConverter;
    private final JwtAccountConverter accountConverter;
    protected final UserInfoExtractor userInfoExtractor;
    protected final String openidScope;

    public QuickcaseAuthenticationConverter(
            JwtClientIdConverter clientIdConverter,
            JwtAccountConverter accountConverter,
            UserInfoExtractor userInfoExtractor,
            String openidScope
    ) {
        this.clientIdConverter = clientIdConverter;
        this.accountConverter = accountConverter;
        this.userInfoExtractor = userInfoExtractor;
        this.openidScope = openidScope;
    }

    @Override
    public QuickcaseAuthentication convert(@NonNull Jwt source) {
        final String clientId = clientIdConverter.convert(source);
        final String account = accountConverter.convert(source);

        final String scopeStr = source.getClaimAsString("scope");

        if (scopeStr == null) {
            throw new OidcException("No scope claim found");
        }

        final Set<String> scopes = Set.of(scopeStr.split(SCOPE_DELIMITER));

        if (scopes.contains(openidScope)) {
            return userAuthentication(source, scopes, clientId, account);
        }

        return clientAuthentication(source, scopes, clientId, account);
    }

    protected QuickcaseAuthentication clientAuthentication(Jwt jwt, Set<String> scopes, String clientId, String account) {
        final String subject = jwt.getSubject();
        return new QuickcaseClientAuthentication(jwt, subject, authorities(scopes), scopes, clientId, account);
    }

    protected QuickcaseAuthentication userAuthentication(Jwt jwt, Set<String> scopes, String clientId, String account) {
        final ClaimsParser claims = new JwtClaimsParser(jwt);
        final UserInfo userInfo = userInfoExtractor.extract(claims);
        return new QuickcaseUserAuthentication(jwt, authorities(scopes, userInfo.getRoles()), userInfo, clientId, account);
    }

    protected final String prefixScope(String scope) {
        return "SCOPE_" + scope;
    }

    protected final String prefixRole(String role) {
        return "ROLE_" + role;
    }

    protected final Set<GrantedAuthority> authorities(Set<String> scopes) {
        return authorities(scopes, Set.of());
    }

    protected final Set<GrantedAuthority> authorities(Set<String> scopes, Set<String> roles) {
        return Stream.concat(
                             scopes.stream().map(this::prefixScope),
                             roles.stream().map(this::prefixRole)
                     )
                     .map(SimpleGrantedAuthority::new)
                     .collect(toSet());
    }
}
