package backend.academy.scrapper.service.apiClient.wrapper;

import backend.academy.scrapper.service.apiClient.GithubClient;
import backend.academy.shared.exceptions.BaseException;
import java.net.URI;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("github.com")
@Slf4j
public class GithubWrapper implements ApiClientWrapper {

    private final GithubClient client;

    @Autowired
    public GithubWrapper(GithubClient client) {
        this.client = client;
    }

    @Override
    public LocalDateTime getLastUpdate(URI uri) {
        String[] path = uri.getPath().split("/");
        if (path.length == 3) {
            return client.getRepositoryUpdate(uri);
        } else if (path.length == 5 && path[3].equals("issues")) {
            return client.getIssueUpdate(uri);
        } else {
            MDC.put("uri", uri.toString());
            log.info("Ресурс не поддерживается");
            MDC.clear();
            throw new BaseException("Ресурс не поддерживается " + uri);
        }
    }
}
