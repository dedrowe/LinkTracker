package backend.academy.scrapper.service.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import backend.academy.scrapper.service.apiClient.GithubClient;
import backend.academy.shared.exceptions.ApiCallException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

public class GithubClientTest {

    private final String mockServerAddress = "localhost";

    private final int mockServerPort = 8080;

    private final WireMockServer wireMockServer = new WireMockServer(
            WireMockConfiguration.options().bindAddress(mockServerAddress).port(mockServerPort));

    private final GithubClient githubClient =
            new GithubClient(RestClient.create("http://" + mockServerAddress + ":" + mockServerPort));

    @BeforeEach
    void setUp() {
        wireMockServer.start();
        configureFor(mockServerAddress, wireMockServer.port());
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    private final String issuesUrl = "https://github.com/-1/-1/issues/-1";

    private final String repositoriesUrl = "https://github.com/-1/-1";

    @Test
    public void getIssuesSuccessTest() {
        String expectedUpdate = "2025-02-13T08:04:58Z";
        String wireMockUrl = "/-1/-1/issues/-1";
        LocalDateTime expectedTime = ZonedDateTime.parse(expectedUpdate).toLocalDateTime();
        stubFor(get(urlPathMatching(wireMockUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("{\"updated_at\": \"" + expectedUpdate + "\"}")));

        LocalDateTime actualDateTime = githubClient.getIssueUpdate(URI.create(issuesUrl));

        verify(getRequestedFor(urlPathMatching(wireMockUrl)));
        assertThat(actualDateTime).isEqualTo(expectedTime);
    }

    @Test
    public void getIssuesFailTest() {
        String expectedDescription = "Ошибка при обращении по ссылке";
        int status = 400;
        stubFor(get(urlPathMatching("/-1/-1/issues/-1"))
                .willReturn(aResponse().withStatus(200)));

        assertThatExceptionOfType(ApiCallException.class)
                .isThrownBy(() -> githubClient.getIssueUpdate(URI.create(issuesUrl)))
                .satisfies(ex -> {
                    assertThat(ex.description()).isEqualTo(expectedDescription);
                    assertThat(ex.getMessage()).isEqualTo(expectedDescription);
                    assertThat(ex.url()).isEqualTo(issuesUrl);
                    assertThat(ex.code()).isEqualTo(status);
                });
    }

    @Test
    public void getRepositorySuccessTest() {
        String expectedUpdate = "2025-02-13T08:04:58Z";
        String wireMockUrl = "/-1/-1";
        LocalDateTime expectedTime = ZonedDateTime.parse(expectedUpdate).toLocalDateTime();
        stubFor(get(urlPathMatching(wireMockUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("{\"updated_at\": \"" + expectedUpdate + "\"}")));

        LocalDateTime actualDateTime = githubClient.getRepositoryUpdate(URI.create(repositoriesUrl));

        verify(getRequestedFor(urlPathMatching(wireMockUrl)));
        assertThat(actualDateTime).isEqualTo(expectedTime);
    }

    @Test
    public void getRepositoryFailTest() {
        String expectedDescription = "Ошибка при обращении по ссылке";
        int status = 400;
        stubFor(get(urlPathMatching("/-1/-1")).willReturn(aResponse().withStatus(200)));

        assertThatExceptionOfType(ApiCallException.class)
                .isThrownBy(() -> githubClient.getRepositoryUpdate(URI.create(repositoriesUrl)))
                .satisfies(ex -> {
                    assertThat(ex.description()).isEqualTo(expectedDescription);
                    assertThat(ex.getMessage()).isEqualTo(expectedDescription);
                    assertThat(ex.url()).isEqualTo(repositoriesUrl);
                    assertThat(ex.code()).isEqualTo(status);
                });
    }

    @Test
    public void responseCode400Test() {
        String expectedDescription = "Ошибка при обращении по ссылке";
        String expectedMessage = "example message";
        int expectedStatusCode = 400;
        String expectedUrl = "https://example.com";
        stubFor(get(urlPathMatching(".*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusCode)
                        .withBody(expectedMessage)));

        assertThatExceptionOfType(ApiCallException.class)
                .isThrownBy(() -> githubClient.getRepositoryUpdate(URI.create(expectedUrl)))
                .satisfies(ex -> {
                    assertThat(ex.description()).isEqualTo(expectedDescription);
                    assertThat(ex.getMessage()).isEqualTo(expectedMessage);
                    assertThat(ex.url()).isEqualTo(expectedUrl);
                    assertThat(ex.code()).isEqualTo(expectedStatusCode);
                });
    }

    @Test
    public void responseCode500Test() {
        String expectedDescription = "Сервис сейчас недоступен";
        int expectedStatusCode = 500;
        String expectedUrl = "https://example.com";
        stubFor(get(urlPathMatching(".*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusCode)));

        assertThatExceptionOfType(ApiCallException.class)
                .isThrownBy(() -> githubClient.getRepositoryUpdate(URI.create(expectedUrl)))
                .satisfies(ex -> {
                    assertThat(ex.description()).isEqualTo(expectedDescription);
                    assertThat(ex.getMessage()).isEqualTo(expectedDescription);
                    assertThat(ex.url()).isEqualTo(expectedUrl);
                    assertThat(ex.code()).isEqualTo(expectedStatusCode);
                });
    }
}
