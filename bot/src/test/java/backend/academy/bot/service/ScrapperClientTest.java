package backend.academy.bot.service;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import backend.academy.bot.service.apiClient.ScrapperClient;
import backend.academy.shared.exceptions.ApiCallException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

public class ScrapperClientTest {

    private final String mockServerAddress = "localhost";

    private final int mockServerPort = 8080;

    private final WireMockServer wireMockServer = new WireMockServer(
            WireMockConfiguration.options().bindAddress(mockServerAddress).port(mockServerPort));

    private final ScrapperClient scrapperClient = new ScrapperClient(
        RestClient.builder()
            .baseUrl("http://" + mockServerAddress + ":" + mockServerPort)
            .build(),
            new ObjectMapper());

    @BeforeEach
    void setUp() {
        wireMockServer.start();
        configureFor(mockServerAddress, wireMockServer.port());
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    public void handle4xxStatusTest() {
        int expectedStatus = 400;
        String expectedUrl = "http://localhost:8080/links?Tg-Chat-Id=1";
        StringBuilder sb = new StringBuilder();
        sb.append("{").append("\"description\": ").append("\"example description\",");
        sb.append("\"exceptionName\": ").append("\"Example\",");
        sb.append("\"exceptionMessage\": ").append("\"example message\",");
        sb.append("\"code\": ").append("\"BAD_REQUEST\"").append(",");
        sb.append("\"stacktrace\": ").append("[]}");
        stubFor(get(urlPathMatching("/links*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(400)
                        .withBody(sb.toString())));

        assertThatExceptionOfType(ApiCallException.class)
                .isThrownBy(() -> scrapperClient.getLinks(1))
                .satisfies(ex -> {
                    assertThat(ex.description()).isEqualTo("example description");
                    assertThat(ex.getMessage()).isEqualTo("example message");
                    assertThat(ex.url()).isEqualTo(expectedUrl);
                    assertThat(ex.code()).isEqualTo(expectedStatus);
                });
    }

    @Test
    public void handle5xxStatusTest() {
        int expectedStatus = 500;
        String expectedUrl = "http://localhost:8080/links?Tg-Chat-Id=1";
        StringBuilder sb = new StringBuilder();
        sb.append("{").append("\"description\": ").append("\"example description\",");
        sb.append("\"exceptionName\": ").append("\"Example\",");
        sb.append("\"exceptionMessage\": ").append("\"example message\",");
        sb.append("\"code\": ").append("\"INTERNAL_SERVER_ERROR\"").append(",");
        sb.append("\"stacktrace\": ").append("[]}");
        stubFor(get(urlPathMatching("/links*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(400)
                        .withBody(sb.toString())));

        assertThatExceptionOfType(ApiCallException.class)
                .isThrownBy(() -> scrapperClient.getLinks(1))
                .satisfies(ex -> {
                    assertThat(ex.description()).isEqualTo("Произошла ошибка");
                    assertThat(ex.getMessage()).isEqualTo("example message");
                    assertThat(ex.url()).isEqualTo(expectedUrl);
                    assertThat(ex.code()).isEqualTo(expectedStatus);
                });
    }
}
