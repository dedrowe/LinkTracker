package backend.academy.scrapper.service.apiClient;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.dto.github.Comment;
import backend.academy.scrapper.dto.github.GHRepository;
import backend.academy.scrapper.dto.github.Issue;
import backend.academy.scrapper.dto.github.PullRequest;
import backend.academy.shared.exceptions.NotRetryApiCallException;
import backend.academy.shared.utils.client.RequestFactoryBuilder;
import backend.academy.shared.utils.client.RetryWrapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.net.URI;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class GithubClient extends ApiClient {

    @Autowired
    public GithubClient(ScrapperConfig config, RestClient.Builder clientBuilder, RetryWrapper wrapper) {
        client = clientBuilder
                .requestFactory(new RequestFactoryBuilder().build())
                .baseUrl(config.github().githubBaseUrl())
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("Authorization", "Bearer " + config.github().githubToken())
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .build();
        retryWrapper = wrapper;
    }

    public GithubClient(RestClient client, RetryWrapper wrapper) {
        this.client = client;
        retryWrapper = wrapper;
    }

    @CircuitBreaker(name = "external-services")
    public Issue getIssue(URI uri) {
        Issue issue = retryWrapper.retry(() -> getRequest(uri).body(Issue.class));
        if (issue == null) {
            throw new NotRetryApiCallException("Ошибка при обращении по ссылке", 400, uri.toString());
        }
        return issue;
    }

    @CircuitBreaker(name = "external-services")
    public List<Issue> getIssues(URI uri) {
        List<Issue> issues = retryWrapper.retry(() -> getRequest(uri).body(new ParameterizedTypeReference<>() {}));
        if (issues == null) {
            throw new NotRetryApiCallException("Ошибка при обращении по ссылке", 400, uri.toString());
        }
        return issues;
    }

    @CircuitBreaker(name = "external-services")
    public List<Comment> getComments(URI uri) {
        List<Comment> comments = retryWrapper.retry(() -> getRequest(uri).body(new ParameterizedTypeReference<>() {}));
        if (comments == null) {
            throw new NotRetryApiCallException("Ошибка при обращении по ссылке", 400, uri.toString());
        }
        return comments;
    }

    @CircuitBreaker(name = "external-services")
    public GHRepository getRepository(URI uri) {
        GHRepository repository = retryWrapper.retry(() -> getRequest(uri).body(GHRepository.class));
        if (repository == null) {
            throw new NotRetryApiCallException("Ошибка при обращении по ссылке", 400, uri.toString());
        }
        return repository;
    }

    @CircuitBreaker(name = "external-services")
    public List<PullRequest> getPullRequests(URI uri) {
        List<PullRequest> pullRequests =
                retryWrapper.retry(() -> getRequest(uri).body(new ParameterizedTypeReference<>() {}));
        if (pullRequests == null) {
            throw new NotRetryApiCallException("Ошибка при обращении по ссылке", 400, uri.toString());
        }
        return pullRequests;
    }
}
