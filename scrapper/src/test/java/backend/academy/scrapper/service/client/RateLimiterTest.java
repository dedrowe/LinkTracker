package backend.academy.scrapper.service.client;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.service.ScrapperContainers;
import jakarta.annotation.PostConstruct;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RateLimiterTest extends ScrapperContainers {

    private final RestClient.Builder builder;

    private RestClient restClient;

    private static final long LIMIT = 10;

    @LocalServerPort
    private int port;

    @Autowired
    public RateLimiterTest(RestClient.Builder builder) {
        this.builder = builder;
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("app.rate-limit.limit", () -> LIMIT);
    }

    @PostConstruct
    void init() {
        this.restClient = builder.baseUrl("http://localhost:" + port).build();
    }

    @Test
    public void rateLimiterTest() {
        String url = "/links?Tg-Chat-Id=1";

        for (int i = 0; i < LIMIT; i++) {
            ResponseEntity<String> response = restClient
                    .get()
                    .uri(URI.create(url))
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, httpResponse) -> {})
                    .toEntity(String.class);
            assertThat(response.getStatusCode().value()).isEqualTo(400);
        }
        ResponseEntity<String> response = restClient
                .get()
                .uri(URI.create(url))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, httpResponse) -> {})
                .toEntity(String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(429);
    }
}
