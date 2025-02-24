package backend.academy.scrapper.service.apiClient;

import backend.academy.shared.exceptions.ApiCallException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;

@Slf4j
public abstract class ApiClient {

    protected RestClient client;

    protected RestClient.ResponseSpec getRequest(URI uri) {
        return setStatusHandlers(client.get().uri(uri.getPath()).retrieve(), uri);
    }

    protected RestClient.ResponseSpec setStatusHandlers(RestClient.ResponseSpec responseSpec, URI uri) {
        return responseSpec
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    throw new ApiCallException(
                            "Ошибка при обращении по ссылке ",
                            body,
                            response.getStatusCode().value(),
                            uri.toString());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new ApiCallException(
                            "Сервис сейчас недоступен", response.getStatusCode().value(), uri.toString());
                });
    }
}
