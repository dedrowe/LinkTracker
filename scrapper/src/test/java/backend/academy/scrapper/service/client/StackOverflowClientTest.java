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

import backend.academy.scrapper.service.apiClient.StackOverflowClient;
import backend.academy.shared.exceptions.ApiCallException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

public class StackOverflowClientTest {

    private final String mockServerAddress = "localhost";

    private final int mockServerPort = 8080;

    private final WireMockServer wireMockServer = new WireMockServer(
            WireMockConfiguration.options().bindAddress(mockServerAddress).port(mockServerPort));

    private final String key = "123";

    private final String accessToken = "123";

    private final StackOverflowClient stackOverflowClient = new StackOverflowClient(
            RestClient.create("http://" + mockServerAddress + ":" + mockServerPort), key, accessToken);

    @BeforeEach
    void setUp() {
        wireMockServer.start();
        configureFor(mockServerAddress, wireMockServer.port());
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    private final String questionsUrl = "https://stackoverflow.com/questions/-1";

    @Test
    public void getQuestionsSuccessTest() {
        long expectedUpdate = 123123L;
        String wireMockUrl = "/questions/-1*";
        LocalDateTime expectedTime =
                Instant.ofEpochSecond(expectedUpdate).atZone(ZoneOffset.UTC).toLocalDateTime();
        stubFor(get(urlPathMatching(wireMockUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("{\"items\": [{\"last_activity_date\": \"" + expectedUpdate + "\"}]}")));

        LocalDateTime actualTime = stackOverflowClient.getQuestionUpdate(URI.create(questionsUrl));

        verify(getRequestedFor(urlPathMatching(wireMockUrl)));
        assertThat(actualTime).isEqualTo(expectedTime);
    }

    @Test
    public void getQuestionsFailTest() {
        String expectedDescription = "Ошибка при обращении по ссылке";
        int status = 400;
        String wireMockUrl = "/questions/-1*";
        stubFor(get(urlPathMatching(wireMockUrl)).willReturn(aResponse().withStatus(200)));

        assertThatExceptionOfType(ApiCallException.class)
                .isThrownBy(() -> stackOverflowClient.getQuestionUpdate(URI.create(questionsUrl)))
                .satisfies(ex -> {
                    assertThat(ex.description()).isEqualTo(expectedDescription);
                    assertThat(ex.getMessage()).isEqualTo(expectedDescription);
                    assertThat(ex.url()).isEqualTo(questionsUrl);
                    assertThat(ex.code()).isEqualTo(status);
                });
    }

    @Test
    public void getWrongQuestionUrlTest() {
        String expectedDescription = "Ошибка при обращении по ссылке";
        int status = 400;
        String wireMockUrl = "/questions/-1*";
        stubFor(get(urlPathMatching(wireMockUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("{\"items\": []}")));

        assertThatExceptionOfType(ApiCallException.class)
                .isThrownBy(() -> stackOverflowClient.getQuestionUpdate(URI.create(questionsUrl)))
                .satisfies(ex -> {
                    assertThat(ex.description()).isEqualTo(expectedDescription);
                    assertThat(ex.getMessage()).isEqualTo(expectedDescription);
                    assertThat(ex.url()).isEqualTo(questionsUrl);
                    assertThat(ex.code()).isEqualTo(status);
                });
    }
}
