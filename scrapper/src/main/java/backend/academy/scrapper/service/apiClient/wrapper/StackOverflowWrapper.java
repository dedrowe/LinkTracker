package backend.academy.scrapper.service.apiClient.wrapper;

import backend.academy.scrapper.service.apiClient.StackOverflowClient;
import backend.academy.shared.exceptions.ApiCallException;
import java.net.URI;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component("stackoverflow.com")
@Slf4j
@RequiredArgsConstructor
public class StackOverflowWrapper implements ApiClientWrapper {

    private final StackOverflowClient apiClient;

    @Override
    public LocalDateTime getLastUpdate(URI uri) {
        String[] path = uri.getPath().split("/");
        if ((path.length == 3 || path.length == 4) && path[1].equals("questions")) {
            return apiClient.getQuestionUpdate(uri);
        } else {
            throw new ApiCallException("Ресурс не поддерживается", 400, uri.toString());
        }
    }
}
