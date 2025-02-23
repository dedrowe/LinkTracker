package backend.academy.scrapper.service.apiClient.wrapper;

import backend.academy.scrapper.service.apiClient.StackOverflowClient;
import backend.academy.shared.exceptions.BaseException;
import java.net.URI;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component("stackoverflow.com")
@Slf4j
@RequiredArgsConstructor
public class StackOverflowWrapper implements ApiClientWrapper {

    private final StackOverflowClient apiClient;

    @Override
    @SuppressWarnings("PMD.UnusedLocalVariable")
    public LocalDateTime getLastUpdate(URI uri) {
        String[] path = uri.getPath().split("/");
        if ((path.length == 3 || path.length == 4) && path[1].equals("questions")) {
            return apiClient.getQuestionUpdate(uri);
        } else {
            String exceptionMessage = "Ресурс не поддерживается ";
            BaseException ex = new BaseException(exceptionMessage + uri);
            try (var var = MDC.putCloseable("uri", uri.toString())) {
                log.info(exceptionMessage, ex);
            }
            throw ex;
        }
    }
}
