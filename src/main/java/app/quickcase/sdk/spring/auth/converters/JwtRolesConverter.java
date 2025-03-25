package app.quickcase.sdk.spring.auth.converters;

import java.util.List;

import app.quickcase.sdk.spring.auth.OidcConfigDefault;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtRolesConverter extends JwtStringSetConverter {
    public JwtRolesConverter() {
        this(OidcConfigDefault.Claims.QC_ROLES);
    }

    public JwtRolesConverter(String rolesClaimName) {
        this(List.of(rolesClaimName), COMMA_DELIMITER);
    }

    public JwtRolesConverter(String rolesClaimName, String roleDelimiter) {
        this(List.of(rolesClaimName), roleDelimiter);
    }

    public JwtRolesConverter(List<String> rolesClaimNames, String roleDelimiter) {
        super(rolesClaimNames, roleDelimiter);
    }
}
