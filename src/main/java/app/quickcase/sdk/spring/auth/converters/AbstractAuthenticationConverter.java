package app.quickcase.sdk.spring.auth.converters;

import java.util.Set;
import java.util.stream.Stream;

import app.quickcase.sdk.spring.auth.OidcException;
import app.quickcase.sdk.spring.auth.QuickcaseAuthentication;
import app.quickcase.sdk.spring.auth.userinfo.UserInfo;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import static java.util.stream.Collectors.toSet;

public abstract class AbstractAuthenticationConverter implements Converter<Jwt, QuickcaseAuthentication> {
    private final JwtClientIdConverter clientIdConverter;
    private final JwtScopesConverter scopesConverter;
    protected final JwtClientInfoConverter clientInfoConverter;
    protected final String openidScope;

    public AbstractAuthenticationConverter(
            JwtClientIdConverter clientIdConverter,
            JwtScopesConverter scopesConverter,
            JwtClientInfoConverter clientInfoConverter,
            String openidScope
    ) {
        this.clientIdConverter = clientIdConverter;
        this.scopesConverter = scopesConverter;
        this.clientInfoConverter = clientInfoConverter;
        this.openidScope = openidScope;
    }

    @NonNull
    @Override
    public QuickcaseAuthentication convert(@NonNull Jwt jwt) {
        var scopes = scopesConverter.convert(jwt);

        if (scopes.isEmpty()) {
            throw new OidcException("No scope claim found");
        }

        var userInfo = scopes.contains(openidScope) ? convertUserInfo(jwt) : clientInfoConverter.convert(jwt);

        return QuickcaseAuthentication.builder(jwt)
                                      .authorities(authorities(scopes, userInfo.getRoles()))
                                      .clientId(clientIdConverter.convert(jwt))
                                      .userInfo(userInfo)
                                      .build();
    }

    protected abstract UserInfo convertUserInfo(@NonNull Jwt jwt);

    private String prefixScope(String scope) {
        return "SCOPE_" + scope;
    }

    private String prefixRole(String role) {
        return "ROLE_" + role;
    }

    private Set<GrantedAuthority> authorities(Set<String> scopes, Set<String> roles) {
        return Stream.concat(
                             scopes.stream().map(this::prefixScope),
                             roles.stream().map(this::prefixRole)
                     )
                     .map(SimpleGrantedAuthority::new)
                     .collect(toSet());
    }
}
