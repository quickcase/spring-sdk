package app.quickcase.sdk.spring.auth;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import app.quickcase.sdk.spring.auth.organisation.OrganisationProfile;
import app.quickcase.sdk.spring.auth.userinfo.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * An {@link org.springframework.security.core.Authentication} instance which represents an authorised client and acts
 * as a container for an authenticated user.
 * <p>
 * On one hand, the {@link QuickcaseAuthentication} instance acts as the intersection of the client + the user. As such, its
 * authorities are composed of the client's scopes and the user's roles. Furthermore, the authentication is primarily
 * intended for Spring Security and thus it follows its patterns like prefixing authorities with `SCOPE_` and `ROLE_`.
 * <p>
 * On the other hand, the {@link UserInfo} instance contained in the authentication represents the user only and its
 * authorities only contain the user roles, not the scopes. The {@link UserInfo} is intended for the consuming application
 * only and is not used by Spring Security. Hence, the Spring Security patterns are not followed and its authorities
 * are not prefixed.
 * <p>
 * Where an authentication is headless (no user), such as an OAuth2 Client Credentials grant, then the client is considered
 * to be the user as well and a {@link UserInfo} instance is constructed as a best effort to represent the client.
 */
@Slf4j
public class QuickcaseAuthentication extends JwtAuthenticationToken {

    @Nullable
    private final String clientId;
    @NonNull
    private final UserInfo userInfo;

    public QuickcaseAuthentication(
            @NonNull Jwt jwt,
            @NonNull Set<GrantedAuthority> authorities,
            @NonNull UserInfo userInfo,
            @Nullable String clientId
    ) {
        super(jwt, authorities, userInfo.getName());
        this.clientId = clientId;
        this.userInfo = userInfo;
    }

    public String getId() {
        return getToken().getSubject();
    }

    public Optional<String> getClientId() {
        return Optional.ofNullable(clientId);
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public String getAccount() {
        return userInfo.getAccount();
    }

    public Set<String> getRoles() {
        return userInfo.getRoles();
    }

    public Set<String> getGroups() {
        return userInfo.getGroups();
    }

    /**
     * @deprecated Access token should not be used for downstream authentication; scheduled for removal in v2.0.0;
     * if needed the token can still be accessed via {@link JwtAuthenticationToken#getToken()}
     */
    @Deprecated(forRemoval = true)
    public String getAccessToken() {
        return getToken().getTokenValue();
    }

    /**
     * @deprecated Organisations deprecated in favour of role-driven authorisation; scheduled for removal in v2.0.0
     */
    @Deprecated(forRemoval = true)
    public OrganisationProfile getOrganisationProfile(String organisationId) {
        return userInfo.getOrganisationProfile(organisationId);
    }

    public static Builder builder(Jwt jwt) {
        return new Builder(jwt);
    }

    public static class Builder {
        private final Jwt jwt;
        private Set<GrantedAuthority> authorities = new HashSet<>();
        private String clientId;
        private UserInfo userInfo;

        public Builder(Jwt jwt) {
            this.jwt = jwt;
        }

        public Builder authorities(Collection<? extends GrantedAuthority> authorities) {
            this.authorities.addAll(authorities);
            return this;
        }

        public Builder authority(GrantedAuthority authority) {
            this.authorities.add(authority);
            return this;
        }

        public Builder authority(String authority) {
            this.authorities.add(new SimpleGrantedAuthority(authority));
            return this;
        }

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder userInfo(UserInfo userInfo) {
            this.userInfo = userInfo;
            return this;
        }

        public QuickcaseAuthentication build() {
            return new QuickcaseAuthentication(
                    jwt,
                    Collections.unmodifiableSet(authorities),
                    userInfo,
                    clientId
            );
        }
    }
}
