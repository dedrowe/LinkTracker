package backend.academy.scrapper.service.apiClient;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.dto.github.GHRepository;
import backend.academy.scrapper.dto.github.Issue;
import backend.academy.shared.exceptions.BaseException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GithubClient extends ApiClient {

    @Autowired
    public GithubClient(ScrapperConfig config) {
        String baseUrl = "https://api.github.com/repos";
        if (!config.githubBaseUrl().equals("${GITHUB_URL}")) {
            baseUrl = config.githubBaseUrl();
        }
        client = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("Authorization", "Bearer " + config.githubToken())
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .build();
    }

    public GithubClient(RestClient client) {
        this.client = client;
    }

    public LocalDateTime getIssueUpdate(URI uri) {
        Issue issue = getRequest(uri).body(Issue.class);
        if (issue == null) {
            throw new BaseException("Ошибка при обращении по ссылке " + uri);
        }
        return ZonedDateTime.parse(issue.updatedAt()).toLocalDateTime();
    }

    public LocalDateTime getRepositoryUpdate(URI uri) {
        GHRepository repository = getRequest(uri).body(GHRepository.class);
        if (repository == null) {
            throw new BaseException("Ошибка при обращении по ссылке " + uri);
        }
        return ZonedDateTime.parse(repository.updatedAt()).toLocalDateTime();
    }
}
