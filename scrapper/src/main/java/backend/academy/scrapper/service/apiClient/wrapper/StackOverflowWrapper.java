package backend.academy.scrapper.service.apiClient.wrapper;

import backend.academy.shared.exceptions.BaseException;
import backend.academy.scrapper.service.apiClient.StackOverflowClient;
import java.net.URI;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("stackoverflow.com")
public class StackOverflowWrapper implements ApiClientWrapper {

    private final StackOverflowClient apiClient;

    @Autowired
    public StackOverflowWrapper(StackOverflowClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public LocalDateTime getLastUpdate(URI uri) {
        String[] path = uri.getPath().split("/");
        if (path.length == 4 && path[2].equals("questions")) {
            return apiClient.getQuestionUpdate(uri);
        } else {
            throw new BaseException("Ресурс не поддерживается " + uri);
        }
    }
}
