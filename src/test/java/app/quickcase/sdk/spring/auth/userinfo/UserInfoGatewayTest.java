package app.quickcase.sdk.spring.auth.userinfo;

import java.net.URI;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("UserInfoGateway")
class UserInfoGatewayTest {
    private static final URI USER_INFO_URI = URI.create("https://oidc.local/userInfo");
    private static final String ACCESS_TOKEN = "access6789";

    private RestTemplate restTemplate;

    private UserInfoGateway userInfoGateway;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        userInfoGateway = new UserInfoGateway(USER_INFO_URI, restTemplate);
    }

    @Test
    void shouldGetUserInfo() {
        final ObjectMapper objectMapper = new ObjectMapper();
        final ObjectNode responseBody = objectMapper.createObjectNode();
        responseBody.set("sub", objectMapper.convertValue("user-123", JsonNode.class));
        final ResponseEntity<JsonNode> response = ResponseEntity.ok(responseBody);
        when(restTemplate.exchange(eq(USER_INFO_URI), eq(HttpMethod.GET), any(HttpEntity.class), eq(JsonNode.class)))
                .thenReturn(response);

        final Map<String, JsonNode> claims = userInfoGateway.getClaims(ACCESS_TOKEN);

        assertAll(
                () -> assertThat(claims.entrySet(), hasSize(1)),
                () -> assertThat(claims.get("sub").textValue(), equalTo("user-123"))
        );
    }
}
