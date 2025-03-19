package app.quickcase.sdk.spring.auth.organisation;

import java.util.Optional;

import app.quickcase.sdk.spring.auth.AccessLevel;
import app.quickcase.sdk.spring.auth.SecurityClassification;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Value
@Builder
@Slf4j
public class OrganisationProfile {
    /**
     * Classification of a user for an organisation. Defaults to PUBLIC, the lowest classification.
     */
    SecurityClassification securityClassification;

    /**
     * Level of access of a user. Defaults to `INDIVIDUAL`, the most restrictive level.
     */
    AccessLevel accessLevel;

    /**
     * Optional. For users with access level `GROUP` only: the group the user belongs to for the
     * organisation.
     */
    String group;

    public Optional<String> getGroup() {
        return Optional.ofNullable(group);
    }

    public static class OrganisationProfileBuilder {
        private SecurityClassification securityClassification = SecurityClassification.PUBLIC;
        private AccessLevel accessLevel = AccessLevel.INDIVIDUAL;

        public OrganisationProfileBuilder securityClassification(SecurityClassification classification) {
            if (null != classification)
                this.securityClassification = classification;
            return this;
        }

        public OrganisationProfileBuilder accessLevel(AccessLevel accessLevel) {
            if (null != accessLevel)
                this.accessLevel = accessLevel;
            return this;
        }

        public OrganisationProfile build() {
            // Can't use access level group if no group defined
            if (AccessLevel.GROUP.equals(accessLevel) && group == null) {
                accessLevel = AccessLevel.INDIVIDUAL;
                log.warn("Usage of Access Level GROUP without group is forbidden, defaulting to INDIVIDUAL");
            }

            return new OrganisationProfile(securityClassification, accessLevel, group);
        }
    }
}
