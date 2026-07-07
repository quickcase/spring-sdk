package app.quickcase.sdk.spring.auth.userinfo;

import java.net.URI;

import app.quickcase.sdk.spring.auth.OidcException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

/**
 * @deprecated UserInfo parsing deprecated; scheduled for removal in v2.0.0
 */
@Deprecated(forRemoval = true)
@Slf4j
public class UserInfoGateway {
    private final URI userInfoUri;
    private final RestTemplate restTemplate;

    public UserInfoGateway(URI userInfoUri, RestTemplate restTemplate) {
        this.userInfoUri = userInfoUri;
        this.restTemplate = restTemplate;
    }

    public ObjectNode getClaims(String accessToken) {
        var requestEntity = new HttpEntity<>(createHeaders(accessToken));
        var response = restTemplate.exchange(
                userInfoUri,
                HttpMethod.GET,
                requestEntity,
                JsonNode.class
        );

        var body = response.getBody();

        if (body == null || !body.isObject()) {
            throw new OidcException("Invalid user info response: expected object but was " + body);
        }

        return (ObjectNode) body;
    }

    private HttpHeaders createHeaders(String accessToken) {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        return headers;
    }
}
