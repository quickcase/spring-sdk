package app.quickcase.sdk.spring.auth;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import app.quickcase.sdk.spring.auth.organisation.OrganisationProfile;
import app.quickcase.sdk.spring.auth.userinfo.UserInfo;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * QuickCase-flavoured authentication. This aims at providing a best-effort in consistency
 * between client-based (client credentials) and user-based (code grant, implicit grant) flows.
 */
public abstract class QuickcaseAuthentication extends JwtAuthenticationToken {

    /**
     * Creates a token with the supplied array of authorities.
     *
     * @param authorities the collection of <tt>GrantedAuthority</tt>s for the principal
     *                    represented by this authentication object.
     */
    public QuickcaseAuthentication(Jwt jwt, Collection<? extends GrantedAuthority> authorities) {
        super(jwt, authorities);
    }

    /**
     * @deprecated Access token should not be used for downstream authentication; scheduled for removal in v2.0.0;
     * if needed the token can still be accessed via {@link JwtAuthenticationToken#getToken()}
     */
    @Deprecated(forRemoval = true)
    public String getAccessToken() {
        return getToken().getTokenValue();
    }

    public abstract String getId();

    public abstract Set<String> getRoles();

    public abstract Set<String> getGroups();

    /**
     * @deprecated Organisations deprecated in favour of role-driven authorisation; scheduled for removal in v2.0.0
     */
    @Deprecated(forRemoval = true)
    public abstract OrganisationProfile getOrganisationProfile(String organisationId);

    public abstract Optional<UserInfo> getUserInfo();
}
