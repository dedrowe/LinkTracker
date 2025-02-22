package backend.academy.scrapper.service.apiClient;

import backend.academy.shared.exceptions.ApiCallException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
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
                    MDC.put("uri", uri.toString());
                    MDC.put("code", response.getStatusCode().toString());
                    log.warn("Ошибка при обращении по ссылке");
                    MDC.clear();
                    throw new ApiCallException(
                            "Ошибка при обращении по ссылке " + uri,
                            body,
                            response.getStatusCode().value());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new ApiCallException(
                            "Сервис по ссылке " + uri + " сейчас недоступен",
                            response.getStatusCode().value());
                });
    }
}
