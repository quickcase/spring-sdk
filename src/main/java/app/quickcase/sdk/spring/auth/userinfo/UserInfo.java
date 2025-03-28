package app.quickcase.sdk.spring.auth.userinfo;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import app.quickcase.sdk.spring.auth.organisation.OrganisationProfile;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Provides QuickCase user information.
 * Hold non-security related user information for QuickCase users such as name and email.
 */
@Slf4j
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString(onlyExplicitlyIncluded = true) // GDPR: Keep names and emails outside of logs
public class UserInfo implements UserDetails {
    @NonNull
    @ToString.Include
    String subject;
    String name;
    String email;
    @Nullable
    @ToString.Include
    String account;
    @NonNull
    @ToString.Include
    Set<GrantedAuthority> authorities;
    @NonNull
    @ToString.Include
    Set<String> roles;
    @NonNull
    @ToString.Include
    Set<String> groups;
    UserPreferences preferences;

    /**
     * @deprecated Organisation profiles are being phased out in favour of fully role-driven authorisation.
     */
    @Deprecated(forRemoval = true)
    @NonNull
    Map<String, OrganisationProfile> organisationProfiles;

    /**
     * @deprecated Organisation profiles are being phased out in favour of fully role-driven authorisation.
     */
    @Deprecated(forRemoval = true)
    @Nullable
    OrganisationProfile defaultProfile;

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return email != null ? email : subject;
    }

    @NonNull
    public String getName() {
        return name != null ? name : subject;
    }

    public Optional<String> getEmail() {
        return Optional.ofNullable(email);
    }

    /**
     * @deprecated Organisations deprecated in favour of role-driven authorisation; scheduled for removal in v2.0.0
     */
    @Deprecated(forRemoval = true)
    public OrganisationProfile getOrganisationProfile(String organisationId) {
        if (organisationProfiles.get(organisationId) != null) {
            return organisationProfiles.get(organisationId);
        }

        log.debug(
                "No profile found for subject `{}` and organisation `{}`, defaulting to {}",
                getSubject(),
                organisationId,
                defaultProfile
        );
        return defaultProfile;
    }

    public static Builder builder(String subject) {
        return new Builder(subject);
    }

    @RequiredArgsConstructor
    public static class Builder {
        private final String subject;
        private String name;
        private String email;
        private String account;
        private Set<GrantedAuthority> authorities = new HashSet<>();
        private Set<String> roles = new HashSet<>();
        private Set<String> groups = new HashSet<>();
        private UserPreferences preferences;
        private final Map<String, OrganisationProfile> organisationProfiles = new TreeMap<>(String::compareToIgnoreCase);
        private OrganisationProfile defaultProfile;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder account(String account) {
            this.account = account;
            return this;
        }

        public Builder authorities(Collection<? extends GrantedAuthority> authorities) {
            this.authorities.addAll(authorities);
            return this;
        }

        public Builder authorities(String... authorities) {
            Arrays.stream(authorities)
                  .map(SimpleGrantedAuthority::new)
                  .forEach(this.authorities::add);
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

        public Builder roles(Set<String> roles) {
            this.roles.addAll(roles);
            return this;
        }

        public Builder roles(String... roles) {
            this.roles.addAll(Arrays.asList(roles));
            return this;
        }

        public Builder role(String role) {
            this.roles.add(role);
            return this;
        }

        public Builder groups(Set<String> groups) {
            this.groups.addAll(groups);
            return this;
        }

        public Builder groups(String... groups) {
            this.groups.addAll(Arrays.asList(groups));
            return this;
        }

        public Builder group(String group) {
            this.groups.add(group);
            return this;
        }

        public Builder preferences(UserPreferences preferences) {
            this.preferences = preferences;
            return this;
        }

        public Builder organisationProfile(String identifier, OrganisationProfile profile) {
            this.organisationProfiles.put(identifier, profile);
            return this;
        }

        public Builder organisationProfiles(Map<String, OrganisationProfile> profiles) {
            this.organisationProfiles.putAll(profiles);
            return this;
        }

        public Builder defaultProfile(OrganisationProfile defaultProfile) {
            this.defaultProfile = defaultProfile;
            return this;
        }

        public UserInfo build() {
            return new UserInfo(
                    subject,
                    name,
                    email,
                    account,
                    authorities,
                    roles,
                    groups,
                    preferences,
                    organisationProfiles,
                    defaultProfile
            );
        }
    }
}
