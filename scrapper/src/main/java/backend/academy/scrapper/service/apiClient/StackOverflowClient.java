package backend.academy.scrapper.service.apiClient;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.dto.stackOverflow.SOResponse;
import backend.academy.shared.exceptions.BaseException;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class StackOverflowClient extends ApiClient {

    @Autowired
    public StackOverflowClient(ScrapperConfig config) {
        String baseUrl = "https://api.stackexchange.com/2.3";
        if (!config.SOBaseUrl().equals("${SO_URL}")) {
            baseUrl = config.SOBaseUrl();
        }
        client = RestClient.create(baseUrl);
    }

    public StackOverflowClient(RestClient client) {
        this.client = client;
    }

    public LocalDateTime getQuestionUpdate(URI uri) {
        SOResponse responseBody = getRequest(uri).body(SOResponse.class);
        if (responseBody == null || responseBody.items().isEmpty()) {
            throw new BaseException("Ошибка при обращении по ссылке " + uri);
        }
        Instant instant = Instant.ofEpochMilli(responseBody.items().getFirst().lastActivityDate());
        return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
