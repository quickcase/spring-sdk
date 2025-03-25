package app.quickcase.sdk.spring.auth.converters;

import java.util.Set;

import app.quickcase.sdk.spring.auth.claims.ClaimNamesProvider;
import app.quickcase.sdk.spring.auth.organisation.OrganisationProfile;
import app.quickcase.sdk.spring.auth.userinfo.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Extract user info for headless access tokens issued for client credentials grant.
 *
 * This broadly follows the logic of {@link JwtUserInfoConverter} with the following notable differences:
 * <ul>
 *     <li>Email address extraction is ignored as not expected</li>
 *     <li>Name is defaulted to "System" when not explicitly provided</li>
 *     <li>Roles are defaulted to scopes when not explicitly provided</li>
 *     <li>Profiles and preferences are ignored as not expected</li>
 *     <li>The default profile has access level `ORGANISATION`</li>
 * </ul>
 */
@Slf4j
public class JwtClientInfoConverter implements Converter<Jwt, UserInfo> {
    private static final String DEFAULT_NAME = "System";

    private final ClaimNamesProvider claimNames;
    private final JwtAccountConverter accountConverter;
    private final JwtScopesConverter scopesConverter;
    private final JwtRolesConverter rolesConverter;
    private final JwtGroupsConverter groupsConverter;

    public JwtClientInfoConverter(
            ClaimNamesProvider claimNames,
            JwtAccountConverter accountConverter,
            JwtScopesConverter scopesConverter,
            JwtRolesConverter rolesConverter,
            JwtGroupsConverter groupsConverter
    ) {
        this.claimNames = claimNames;
        this.accountConverter = accountConverter;
        this.scopesConverter = scopesConverter;
        this.rolesConverter = rolesConverter;
        this.groupsConverter = groupsConverter;
    }

    @NonNull
    @Override
    public UserInfo convert(@NonNull Jwt jwt) {
        return UserInfo.builder(jwt.getSubject())
                       .name(convertName(jwt))
                       .account(accountConverter.convert(jwt))
                       .roles(convertRoles(jwt))
                       .groups(groupsConverter.convert(jwt))
                       .defaultProfile(OrganisationProfile.DEFAULT_CLIENT_PROFILE)
                       .build();
    }

    /**
     * Defaults roles to scopes when not defined, this is a legacy behaviour preserved for backward compatibility.
     *
     * @param jwt authentication token
     * @return Roles when defined and not empty; otherwise scopes
     */
    private Set<String> convertRoles(Jwt jwt) {
        var roles = rolesConverter.convert(jwt);
        return roles.isEmpty() ? scopesConverter.convert(jwt) : roles;
    }

    /**
     * Defaults name to "System" when not defined, this is a legacy behaviour preserved for backward compatibility.
     * @param jwt authentication token
     * @return Name claim value when defined; otherwise "System"
     */
    private String convertName(Jwt jwt) {
        var name = jwt.getClaimAsString(claimNames.name());
        return name != null ? name : DEFAULT_NAME;
    }
}
