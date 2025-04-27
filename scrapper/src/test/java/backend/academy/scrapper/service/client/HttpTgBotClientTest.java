package backend.academy.scrapper.service.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.mockito.Mockito.times;

import backend.academy.scrapper.service.botClient.HttpTgBotClient;
import backend.academy.scrapper.service.botClient.TgBotClient;
import backend.academy.shared.dto.LinkUpdate;
import backend.academy.shared.utils.client.RetryWrapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
public class HttpTgBotClientTest {

    private final String mockServerAddress = "localhost";

    private final int mockServerPort = 8080;

    private final WireMockServer wireMockServer = new WireMockServer(
            WireMockConfiguration.options().bindAddress(mockServerAddress).port(mockServerPort));

    private final TgBotClient tgBotClient =
            new HttpTgBotClient(RestClient.create("http://" + mockServerAddress + ":" + mockServerPort), new RetryWrapper());

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
    public void sendUpdatesCode4xxTest() {
        LinkUpdate update = new LinkUpdate(1L, "", "", List.of());
        String expectedBody = "example body";
        HttpStatus expectedStatus = HttpStatus.BAD_REQUEST;
        stubFor(post(urlPathMatching("/updates"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatus.value())
                        .withBody(expectedBody)));

        try (MockedStatic<MDC> context = Mockito.mockStatic(MDC.class)) {

            tgBotClient.sendUpdates(update);

            context.verify(() -> MDC.put("code", expectedStatus.toString()), times(1));
            context.verify(() -> MDC.remove("code"), times(1));
        }
    }

    @Test
    public void sendUpdatesCode5xxTest() {
        LinkUpdate update = new LinkUpdate(1L, "", "", List.of());
        String expectedBody = "example body";
        HttpStatus expectedStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        stubFor(post(urlPathMatching("/updates"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatus.value())
                        .withBody(expectedBody)));

        try (MockedStatic<MDC> context = Mockito.mockStatic(MDC.class)) {

            tgBotClient.sendUpdates(update);

            context.verify(() -> MDC.put("code", expectedStatus.toString()), times(1));
            context.verify(() -> MDC.remove("code"), times(1));
        }
    }
}
