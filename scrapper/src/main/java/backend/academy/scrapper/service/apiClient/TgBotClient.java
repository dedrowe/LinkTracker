package backend.academy.scrapper.service.apiClient;

import static backend.academy.shared.utils.client.RetryWrapper.retry;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.shared.dto.LinkUpdate;
import backend.academy.shared.utils.client.RequestFactoryBuilder;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class TgBotClient {

    private final RestClient client;

    @Autowired
    public TgBotClient(ScrapperConfig config, RestClient.Builder clientBuilder) {
        client = clientBuilder
                .requestFactory(new RequestFactoryBuilder().build())
                .baseUrl(config.bot().url())
                .build();
    }

    public TgBotClient(RestClient client) {
        this.client = client;
    }

    @Async
    public void sendUpdates(LinkUpdate updates) {
        retry(() -> client.post()
                .uri("/updates")
                .body(updates)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    MDC.put("code", response.getStatusCode().toString());
                    log.error(body);
                    MDC.remove("code");
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    MDC.put("code", response.getStatusCode().toString());
                    log.error(body);
                    MDC.remove("code");
                })
                .toBodilessEntity());
    }
}
