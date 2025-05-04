package backend.academy.scrapper.service.botClient;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.shared.dto.LinkUpdate;
import backend.academy.shared.utils.client.RequestFactoryBuilder;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class HttpTgBotClient implements TgBotClient {

    private final RestClient client;

    @Autowired
    public HttpTgBotClient(ScrapperConfig config, RestClient.Builder clientBuilder) {
        client = clientBuilder
                .requestFactory(new RequestFactoryBuilder()
                    .setConnectionTimeout(config.timeout().connection())
                    .setReadTimeout(config.timeout().read())
                    .build())
                .baseUrl(config.bot().url())
                .build();
    }

    public HttpTgBotClient(RestClient client) {
        this.client = client;
    }

    @Override
    public void sendUpdates(LinkUpdate updates) {
        client.post()
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
                .toBodilessEntity();
    }
}
