package backend.academy.scrapper.service.client;

import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;

import backend.academy.scrapper.service.apiClient.GithubClient;
import backend.academy.shared.exceptions.ApiCallException;
import backend.academy.shared.exceptions.NotRetryApiCallException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import java.net.URI;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
public class CircuitBreakerTest {

    private final GithubClient githubClient;

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    private static final String mockServerAddress = "localhost";

    private static final int mockServerPort = 8080;

    private final WireMockServer wireMockServer = new WireMockServer(
        WireMockConfiguration.options().bindAddress(mockServerAddress).port(mockServerPort));

    private final String issuesUrl = "https://github.com/-1/-1/issues";

    private static final int retriesCount = 3;

    @Autowired
    public CircuitBreakerTest(GithubClient githubClient, CircuitBreakerRegistry circuitBreakerRegistry) {
        this.githubClient = githubClient;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {

        registry.add("app.github.github-base-url", () -> "http://" + mockServerAddress + ":" + mockServerPort);
        registry.add("resilience4j.circuitbreaker.instances.external-services.sliding-window-size", () -> retriesCount);
        registry.add("resilience4j.circuitbreaker.instances.external-services.minimum-number-of-calls", () -> retriesCount);
    }

    @BeforeEach
    void setUp() {
        wireMockServer.start();
        configureFor(mockServerAddress, wireMockServer.port());
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @ParameterizedTest
    @ValueSource(strings = {"500", "429"})
    public void circuitBreakerTest(int code) {
        String wireMockUrl = "/-1/-1/issues";
        stubFor(get(urlPathMatching(wireMockUrl))
            .willReturn(aResponse()
                .withStatus(code)
                .withBody("error")
                .withFixedDelay(500))
        );
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("external-services");
        circuitBreaker.reset();

        for (int i = 0; i < retriesCount; i++) {
            assertThatThrownBy(() -> githubClient.getIssues(URI.create(issuesUrl))).isInstanceOf(ApiCallException.class);
        }
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
        assertThatThrownBy(() -> githubClient.getIssues(URI.create(issuesUrl))).isInstanceOf(CallNotPermittedException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"400", "403"})
    public void circuitBreakerNotWork4xxResponseTest(int code) {
        String wireMockUrl = "/-1/-1/issues";
        stubFor(get(urlPathMatching(wireMockUrl))
            .willReturn(aResponse()
                .withStatus(code)
                .withBody("error")
                .withFixedDelay(500))
        );
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("external-services");
        circuitBreaker.reset();

        for (int i = 0; i < retriesCount; i++) {
            assertThatThrownBy(() -> githubClient.getIssues(URI.create(issuesUrl))).isInstanceOf(NotRetryApiCallException.class);
        }
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        assertThatThrownBy(() -> githubClient.getIssues(URI.create(issuesUrl))).isInstanceOf(NotRetryApiCallException.class);
    }
}
