package backend.academy.scrapper.service.botClient;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.shared.dto.LinkUpdate;
import backend.academy.shared.utils.client.RequestFactoryBuilder;
import java.nio.charset.StandardCharsets;
import backend.academy.shared.utils.client.RetryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
@ConditionalOnProperty(havingValue = "http", prefix = "app", name = "transport")
public class HttpTgBotClient implements TgBotClient {

    private final RestClient client;

    private final RetryWrapper retryWrapper;

    @Autowired
    public HttpTgBotClient(ScrapperConfig config, RestClient.Builder clientBuilder,
                           RetryWrapper retryWrapper) {
        client = clientBuilder
                .requestFactory(new RequestFactoryBuilder().build())
                .baseUrl(config.bot().url())
                .build();
        this.retryWrapper = retryWrapper;
    }

    public HttpTgBotClient(RestClient client, RetryWrapper retryWrapper) {
        this.client = client;
        this.retryWrapper = retryWrapper;
    }

    @Async
    @Override
    public void sendUpdates(LinkUpdate updates) {
        retryWrapper.retry(() -> client.post()
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
