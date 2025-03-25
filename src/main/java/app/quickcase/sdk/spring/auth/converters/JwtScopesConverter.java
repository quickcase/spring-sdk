package app.quickcase.sdk.spring.auth.converters;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtScopesConverter extends JwtStringSetConverter {
    private static final List<String> SCOPE_CLAIM_NAMES = List.of("scope", "scp");

    public JwtScopesConverter() {
        super(SCOPE_CLAIM_NAMES, SPACE_DELIMITER);
    }
}
