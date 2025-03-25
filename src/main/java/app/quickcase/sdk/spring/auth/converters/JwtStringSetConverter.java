package app.quickcase.sdk.spring.auth.converters;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.jwt.Jwt;

@Slf4j
public class JwtStringSetConverter implements Converter<Jwt, Set<String>> {
    public static final String SPACE_DELIMITER = " ";
    public static final String COMMA_DELIMITER = ",";
    public static final String DEFAULT_DELIMITER = SPACE_DELIMITER;

    private final List<String> claimNames;

    private final String delimiter;

    public JwtStringSetConverter(String claimName) {
        this(List.of(claimName));
    }

    public JwtStringSetConverter(List<String> claimNames) {
        this(claimNames, DEFAULT_DELIMITER);
    }

    public JwtStringSetConverter(List<String> claimNames, String delimiter) {
        this.claimNames = claimNames;
        this.delimiter = delimiter;
    }

    @NonNull
    @Override
    public Set<String> convert(@NonNull Jwt jwt) {
        var claimName = getClaimName(jwt);

        return claimName.map((name) -> {
                            log.debug("Extracting from claim {}", claimName);
                            return jwt.getClaimAsString(name);
                        })
                        .map(groupsStr -> Set.of(groupsStr.split(delimiter)))
                        .orElse(Set.of());
    }

    private Optional<String> getClaimName(Jwt jwt) {
        return claimNames.stream().filter(claimName -> jwt.getClaim(claimName) != null).findFirst();
    }
}
