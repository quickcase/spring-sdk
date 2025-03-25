package app.quickcase.sdk.spring.auth.converters;

import java.util.List;

import app.quickcase.sdk.spring.auth.OidcConfigDefault;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtGroupsConverter extends JwtStringSetConverter {
    public JwtGroupsConverter() {
        this(OidcConfigDefault.Claims.QC_GROUPS);
    }

    public JwtGroupsConverter(String groupsClaimName) {
        this(List.of(groupsClaimName), COMMA_DELIMITER);
    }

    public JwtGroupsConverter(String groupsClaimName, String groupDelimiter) {
        this(List.of(groupsClaimName), groupDelimiter);
    }

    public JwtGroupsConverter(List<String> groupsClaimNames, String groupDelimiter) {
        super(groupsClaimNames, groupDelimiter);
    }
}
