package backend.academy.scrapper.service.apiClient;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.dto.github.GHRepository;
import backend.academy.scrapper.dto.github.Issue;
import backend.academy.shared.exceptions.ApiCallException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class GithubClient extends ApiClient {

    @Autowired
    public GithubClient(ScrapperConfig config) {
        client = RestClient.builder()
                .baseUrl(config.github().githubBaseUrl())
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("Authorization", "Bearer " + config.github().githubToken())
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .build();
    }

    public GithubClient(RestClient client) {
        this.client = client;
    }

    public LocalDateTime getIssueUpdate(URI uri) {
        Issue issue = getRequest(uri).body(Issue.class);
        if (issue == null) {
            throw new ApiCallException("Ошибка при обращении по ссылке", 400, uri.toString());
        }
        return ZonedDateTime.parse(issue.updatedAt()).toLocalDateTime();
    }

    public LocalDateTime getRepositoryUpdate(URI uri) {
        GHRepository repository = getRequest(uri).body(GHRepository.class);
        if (repository == null) {
            throw new ApiCallException("Ошибка при обращении по ссылке", 400, uri.toString());
        }
        return ZonedDateTime.parse(repository.updatedAt()).toLocalDateTime();
    }
}
