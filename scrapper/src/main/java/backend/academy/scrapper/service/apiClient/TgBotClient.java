package backend.academy.scrapper.service.apiClient;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.shared.dto.LinkUpdate;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class TgBotClient {

    private final RestClient client;

    @Autowired
    public TgBotClient(ScrapperConfig config) {
        String baseUrl = "http://localhost:8080";
        if (!config.botUrl().equals("${BOT_URL}")) {
            baseUrl = config.botUrl();
        }
        client = RestClient.create(baseUrl);
    }

    public TgBotClient(RestClient client) {
        this.client = client;
    }

    public void sendUpdates(LinkUpdate updates) {
        client.post()
                .uri("/updates")
                .body(updates)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    MDC.put("code", response.getStatusCode().toString());
                    log.error(body);
                    MDC.clear();
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    MDC.put("code", response.getStatusCode().toString());
                    log.error(body);
                    MDC.clear();
                })
                .toBodilessEntity();
    }
}
