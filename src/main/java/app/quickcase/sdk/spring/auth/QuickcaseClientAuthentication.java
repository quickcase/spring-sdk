package app.quickcase.sdk.spring.auth;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import app.quickcase.sdk.spring.auth.organisation.OrganisationProfile;
import app.quickcase.sdk.spring.auth.userinfo.UserInfo;
import lombok.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

public class QuickcaseClientAuthentication extends QuickcaseAuthentication {
    private static final String DEFAULT_NAME = "System";
    private static final OrganisationProfile ORGANISATION_PROFILE = clientProfile();

    private static OrganisationProfile clientProfile() {
        return OrganisationProfile.builder()
                                  .accessLevel(AccessLevel.ORGANISATION)
                                  .securityClassification(SecurityClassification.PUBLIC)
                                  .build();
    }

    private final String subject;
    private final Set<String> roles;
    @Nullable
    private final String clientId;
    @Nullable
    private final String account;

    public QuickcaseClientAuthentication(
            @NonNull Jwt jwt,
            @NonNull String subject,
            @NonNull Collection<? extends GrantedAuthority> authorities,
            @NonNull Set<String> roles,
            @Nullable String clientId,
            @Nullable String account
    ) {
        super(jwt, authorities);
        this.subject = subject;
        this.roles = roles;
        this.clientId = clientId;
        this.account = account;
        this.setAuthenticated(true);
    }

    @Override
    public String getId() {
        return subject;
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
        return DEFAULT_NAME;
    }

    @Override
    public Set<String> getRoles() {
        return roles;
    }

    @Override
    public Set<String> getGroups() {
        return Set.of();
    }

    /**
     * @deprecated Organisation profiles are being phased out in favour of fully role-driven authorisation.
     */
    @Deprecated(forRemoval = true)
    @Override
    public OrganisationProfile getOrganisationProfile(String organisationId) {
        return ORGANISATION_PROFILE;
    }

    @Override
    public Optional<UserInfo> getUserInfo() {
        return Optional.empty();
    }
}
