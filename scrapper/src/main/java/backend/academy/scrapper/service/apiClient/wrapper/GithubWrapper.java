package backend.academy.scrapper.service.apiClient.wrapper;

import backend.academy.scrapper.service.apiClient.GithubClient;
import backend.academy.shared.exceptions.ApiCallException;
import java.net.URI;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component("github.com")
@Slf4j
@RequiredArgsConstructor
public class GithubWrapper implements ApiClientWrapper {

    private final GithubClient client;

    @Override
    public LocalDateTime getLastUpdate(URI uri) {
        String[] path = uri.getPath().split("/");
        if (path.length == 3) {
            return client.getRepositoryUpdate(uri);
        } else if (path.length == 5 && path[3].equals("issues")) {
            return client.getIssueUpdate(uri);
        } else {
            throw new ApiCallException("Ресурс не поддерживается", 400, uri.toString());
        }
    }
}
