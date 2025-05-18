package backend.academy.scrapper.service.apiClient;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.dto.stackOverflow.Question;
import backend.academy.scrapper.dto.stackOverflow.SOResponse;
import backend.academy.shared.exceptions.NotRetryApiCallException;
import backend.academy.shared.utils.client.RequestFactoryBuilder;
import backend.academy.shared.utils.client.RetryWrapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class StackOverflowClient extends ApiClient {

    private final String key;

    private final String accessToken;

    /**
     * Апи stackoverflow предоставляет возможность настраивать фильтры, чтобы добавлять и удалять поля из возвращаемый
     * сущностей. Это позволяет уменьшить передаваемый трафик и упростить обработку полученного результата. Подробнее
     * можно почитать по <a href="https://api.stackexchange.com/docs/filters">ссылке</a>.
     */
    private static final String REQUEST_FILTER = "!LbeNt-eYI5wF9dcYOL_10T";

    @Autowired
    public StackOverflowClient(ScrapperConfig config, RestClient.Builder clientBuilder, RetryWrapper wrapper) {
        key = config.stackOverflow().key();
        accessToken = config.stackOverflow().accessToken();
        client = clientBuilder
                .requestFactory(new RequestFactoryBuilder()
                        .setConnectionTimeout(config.timeout().connection())
                        .setReadTimeout(config.timeout().read())
                        .build())
                .baseUrl(config.stackOverflow().SOBaseUrl())
                .build();
        retryWrapper = wrapper;
    }

    public StackOverflowClient(RestClient client, String key, String accessToken, RetryWrapper wrapper) {
        this.client = client;
        this.key = key;
        this.accessToken = accessToken;
        retryWrapper = wrapper;
    }

    @CircuitBreaker(name = "external-services")
    public Question getQuestionUpdate(URI uri) {
        SOResponse responseBody = retryWrapper.retry(() -> getRequest(uri).body(SOResponse.class));
        if (responseBody == null || responseBody.items().isEmpty()) {
            throw new NotRetryApiCallException("Ошибка при обращении по ссылке", 400, uri.toString());
        }
        return responseBody.items().getFirst();
    }

    @Override
    @SuppressWarnings("StringSplitter")
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
                                .queryParam("filter", REQUEST_FILTER)
                                .build())
                        .retrieve(),
                uri);
    }
}
