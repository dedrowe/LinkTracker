package backend.academy.scrapper.service.apiClient;

import static backend.academy.shared.utils.client.RetryWrapper.retry;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.dto.stackOverflow.SOResponse;
import backend.academy.shared.exceptions.ApiCallException;
import backend.academy.shared.utils.client.RequestFactoryBuilder;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class StackOverflowClient extends ApiClient {

    private final String key;

    private final String accessToken;

    @Autowired
    public StackOverflowClient(ScrapperConfig config) {
        key = config.stackOverflow().key();
        accessToken = config.stackOverflow().accessToken();
        client = RestClient.builder()
                .requestFactory(new RequestFactoryBuilder().build())
                .baseUrl(config.stackOverflow().SOBaseUrl())
                .build();
    }

    public StackOverflowClient(RestClient client, String key, String accessToken) {
        this.client = client;
        this.key = key;
        this.accessToken = accessToken;
    }

    public LocalDateTime getQuestionUpdate(URI uri) {
        SOResponse responseBody = retry(() -> getRequest(uri).body(SOResponse.class));
        if (responseBody == null || responseBody.items().isEmpty()) {
            throw new ApiCallException("Ошибка при обращении по ссылке", 400, uri.toString());
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
