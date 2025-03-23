package app.quickcase.sdk.spring.auth;

import java.util.Optional;
import java.util.Set;

import app.quickcase.sdk.spring.auth.organisation.OrganisationProfile;
import app.quickcase.sdk.spring.auth.userinfo.UserInfo;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

@Slf4j
public class QuickcaseUserAuthentication extends QuickcaseAuthentication {
    public static final OrganisationProfile DEFAULT_PROFILE = OrganisationProfile.DEFAULT_USER_PROFILE;

    private final UserInfo userInfo;
    @Nullable
    private final String clientId;
    @Nullable
    private final String account;

    public QuickcaseUserAuthentication(
            @NonNull Jwt jwt,
            @NonNull Set<GrantedAuthority> authorities,
            @NonNull UserInfo userInfo,
            @Nullable String clientId,
            @Nullable String account
    ) {
        super(jwt, authorities);
        this.userInfo = userInfo;
        this.clientId = clientId;
        this.account = account;
        this.setAuthenticated(true);
    }

    @Override
    public String getId() {
        return userInfo.getSubject();
    }

    @Override
    public Optional<String> getClientId() {
        return Optional.ofNullable(clientId);
    }

    @Override
    public String getAccount() {
        return account;
    }

    @Override
    public String getName() {
        return userInfo.getName();
    }

    @Override
    public Set<String> getRoles() {
        return userInfo.getRoles();
    }

    @Override
    public Set<String> getGroups() {
        return userInfo.getGroups();
    }

    /**
     * @deprecated Organisation profiles are being phased out in favour of fully role-driven authorisation.
     */
    @Deprecated(forRemoval = true)
    @Override
    public OrganisationProfile getOrganisationProfile(String organisationId) {
        return Optional.ofNullable(userInfo.getOrganisationProfiles().get(organisationId))
                       .orElseGet(() -> {
                           log.debug(
                                   "No profile found for user `{}` and organisation `{}`, " +
                                           "defaulting to PUBLIC/INDIVIDUAL",
                                   getId(),
                                   organisationId);
                           return DEFAULT_PROFILE;
                       });
    }

    @Override
    public Optional<UserInfo> getUserInfo() {
        return Optional.of(userInfo);
    }
}
