package backend.academy.scrapper.service.apiClient;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.dto.stackOverflow.SOResponse;
import backend.academy.shared.exceptions.BaseException;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class StackOverflowClient extends ApiClient {

    private String key;

    private String accessToken;

    @Autowired
    public StackOverflowClient(ScrapperConfig config) {
        key = config.stackOverflow().key();
        accessToken = config.stackOverflow().accessToken();
        String baseUrl = "https://api.stackexchange.com/2.3";
        if (!config.SOBaseUrl().equals("${SO_URL}")) {
            baseUrl = config.SOBaseUrl();
        }
        client = RestClient.create(baseUrl);
    }

    public StackOverflowClient(RestClient client) {
        this.client = client;
    }

    @SuppressWarnings("PMD.UnusedLocalVariable")
    public LocalDateTime getQuestionUpdate(URI uri) {
        SOResponse responseBody = getRequest(uri).body(SOResponse.class);
        if (responseBody == null || responseBody.items().isEmpty()) {
            try (var var = MDC.putCloseable("url", uri.toString())) {
                log.error("Вопрос не найден");
            }
            throw new BaseException("Ошибка при обращении по ссылке " + uri);
        }
        Instant instant = Instant.ofEpochSecond(responseBody.items().getFirst().lastActivityDate());
        return instant.atZone(ZoneOffset.UTC).toLocalDateTime();
    }

    @Override
    protected RestClient.ResponseSpec getRequest(URI uri) {
        String[] pathTokens = uri.getPath().split("/");
        String path;
        if (pathTokens.length == 4) {
            path = pathTokens[0] + "/" + pathTokens[1] + "/" + pathTokens[2];
        } else {
            path = uri.getPath();
        }
        String finalPath = path;
        return setStatusHandlers(
                client.get()
                        .uri(uriBuilder -> uriBuilder
                                .path(finalPath)
                                .queryParam("key", key)
                                .queryParam("access_token", accessToken)
                                .queryParam("site", "stackoverflow")
                                .build())
                        .retrieve(),
                uri);
    }
}
