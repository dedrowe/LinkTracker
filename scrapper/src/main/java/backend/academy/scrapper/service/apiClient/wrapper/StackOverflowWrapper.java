package backend.academy.scrapper.service.apiClient.wrapper;

import backend.academy.scrapper.service.apiClient.StackOverflowClient;
import backend.academy.shared.exceptions.BaseException;
import java.net.URI;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("stackoverflow.com")
@Slf4j
public class StackOverflowWrapper implements ApiClientWrapper {

    private final StackOverflowClient apiClient;

    @Autowired
    public StackOverflowWrapper(StackOverflowClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public LocalDateTime getLastUpdate(URI uri) {
        String[] path = uri.getPath().split("/");
        if ((path.length == 3 || path.length == 4) && path[1].equals("questions")) {
            return apiClient.getQuestionUpdate(uri);
        } else {
            MDC.put("uri", uri.toString());
            log.info("Ресурс не поддерживается");
            MDC.clear();
            throw new BaseException("Ресурс не поддерживается " + uri);
        }
    }
}
