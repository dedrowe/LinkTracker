package backend.academy.scrapper.service.apiClient;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.dto.github.GHRepository;
import backend.academy.scrapper.dto.github.Issue;
import backend.academy.shared.exceptions.BaseException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
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

    @SuppressWarnings("PMD.UnusedLocalVariable")
    public LocalDateTime getIssueUpdate(URI uri) {
        Issue issue = getRequest(uri).body(Issue.class);
        if (issue == null) {
            try (var var = MDC.putCloseable("url", uri.toString())) {
                log.error("Issue не найден");
            }
            throw new BaseException("Ошибка при обращении по ссылке " + uri);
        }
        return ZonedDateTime.parse(issue.updatedAt()).toLocalDateTime();
    }

    @SuppressWarnings("PMD.UnusedLocalVariable")
    public LocalDateTime getRepositoryUpdate(URI uri) {
        GHRepository repository = getRequest(uri).body(GHRepository.class);
        if (repository == null) {
            try (var var = MDC.putCloseable("url", uri.toString())) {
                log.error("Репозиторий не найден");
            }
            throw new BaseException("Ошибка при обращении по ссылке " + uri);
        }
        return ZonedDateTime.parse(repository.updatedAt()).toLocalDateTime();
    }
}
