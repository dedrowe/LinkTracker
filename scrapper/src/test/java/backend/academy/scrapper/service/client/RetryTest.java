package backend.academy.scrapper.service.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getAllScenarios;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import backend.academy.shared.exceptions.ApiCallException;
import backend.academy.shared.utils.client.RetryWrapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

@SpringBootTest(classes = {RetryWrapper.class})
@EnableRetry
public class RetryTest {

    private final String mockServerAddress = "localhost";

    private final int mockServerPort = 8080;

    private final WireMockServer wireMockServer = new WireMockServer(
            WireMockConfiguration.options().bindAddress(mockServerAddress).port(mockServerPort));

    private final String issuesUrl = "https://github.com/-1/-1/issues";

    private final RetryWrapper retryWrapper;

    private final RestClient client;

    @Autowired
    public RetryTest(RetryWrapper retryWrapper) {
        this.retryWrapper = retryWrapper;
        client = RestClient.create("http://" + mockServerAddress + ":" + mockServerPort);
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("app.retry.max-attempts", () -> "3");
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

    private String getRequest(URI uri) {
        return client.get()
                .uri(uri.getPath())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    throw new ApiCallException(
                            "Ошибка при обращении по ссылке",
                            body,
                            response.getStatusCode().value(),
                            uri.toString());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new ApiCallException(
                            "Сервис сейчас недоступен", response.getStatusCode().value(), uri.toString());
                })
                .body(String.class);
    }

    @Test
    public void retryOnExceptionTest() {
        String scenarioName = "retryTest";
        String firstAttemptState = "First Attempt";
        String secondAttemptState = "Second Attempt";
        String thirdAttemptState = "Third Attempt";
        String wireMockUrl = "/-1/-1/issues";
        String expectedResult = "test";
        stubFor(get(urlPathMatching(wireMockUrl))
                .inScenario(scenarioName)
                .whenScenarioStateIs(Scenario.STARTED)
                .willSetStateTo(firstAttemptState)
                .willReturn(aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .withBody("Internal Server Error")));
        stubFor(get(urlPathMatching(wireMockUrl))
                .inScenario(scenarioName)
                .whenScenarioStateIs(firstAttemptState)
                .willSetStateTo(secondAttemptState)
                .willReturn(aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .withBody("Internal Server Error")));
        stubFor(get(urlPathMatching(wireMockUrl))
                .inScenario(scenarioName)
                .whenScenarioStateIs(secondAttemptState)
                .willSetStateTo(thirdAttemptState)
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(HttpStatus.OK.value())
                        .withBody(expectedResult)));

        String actualResult = retryWrapper.retry(() -> getRequest(URI.create(issuesUrl)));
        List<Scenario> scenarios = getAllScenarios();

        assertThat(actualResult).isEqualTo(expectedResult);
        assertThat(scenarios.getFirst().getState()).isEqualTo(thirdAttemptState);
    }

    @Test
    public void notRetryOnExceptionTest() {
        String wireMockUrl = "/-1/-1/issues";
        String scenarioName = "retryTest";
        String firstAttemptState = "First Attempt";
        String secondAttemptState = "Second Attempt";
        String expectedResult = "test";
        stubFor(get(urlPathMatching(wireMockUrl))
                .inScenario(scenarioName)
                .whenScenarioStateIs(Scenario.STARTED)
                .willSetStateTo(firstAttemptState)
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withBody("Bad Request")));
        stubFor(get(urlPathMatching(wireMockUrl))
                .inScenario(scenarioName)
                .whenScenarioStateIs(firstAttemptState)
                .willSetStateTo(secondAttemptState)
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(HttpStatus.OK.value())
                        .withBody(expectedResult)));

        assertThatThrownBy(() -> retryWrapper.retry(() -> getRequest(URI.create(issuesUrl))))
                .isInstanceOf(ApiCallException.class);
        List<Scenario> scenarios = getAllScenarios();
        assertThat(scenarios.getFirst().getState()).isEqualTo(firstAttemptState);
    }

    @Test
    public void retryMaxAttemptsTest() {
        String scenarioName = "retryTest";
        String firstAttemptState = "First Attempt";
        String secondAttemptState = "Second Attempt";
        String thirdAttemptState = "Third Attempt";
        String wireMockUrl = "/-1/-1/issues";
        stubFor(get(urlPathMatching(wireMockUrl))
                .inScenario(scenarioName)
                .whenScenarioStateIs(Scenario.STARTED)
                .willSetStateTo(firstAttemptState)
                .willReturn(aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .withBody("Internal Server Error")));
        stubFor(get(urlPathMatching(wireMockUrl))
                .inScenario(scenarioName)
                .whenScenarioStateIs(firstAttemptState)
                .willSetStateTo(secondAttemptState)
                .willReturn(aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .withBody("Internal Server Error")));
        stubFor(get(urlPathMatching(wireMockUrl))
                .inScenario(scenarioName)
                .whenScenarioStateIs(secondAttemptState)
                .willSetStateTo(thirdAttemptState)
                .willReturn(aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .withBody("Internal Server Error")));

        assertThatThrownBy(() -> retryWrapper.retry(() -> getRequest(URI.create(issuesUrl))))
                .isInstanceOf(ApiCallException.class);
        List<Scenario> scenarios = getAllScenarios();
        assertThat(scenarios.getFirst().getState()).isEqualTo(thirdAttemptState);
    }
}
