package backend.academy.scrapper.service.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.dto.github.GHRepository;
import backend.academy.scrapper.dto.github.Issue;
import backend.academy.scrapper.service.apiClient.GithubClient;
import backend.academy.shared.exceptions.BaseException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
public class GithubClientTest {

    private final RestClient client = mock(RestClient.class);

    private final GithubClient githubClient = new GithubClient(client);

    private final RestClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);

    private final RestClient.RequestHeadersSpec requestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);

    private final RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

    @BeforeEach
    void setUp() {

        when(client.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    }

    @Test
    public void getIssuesSuccessTest() {
        Issue issue = new Issue();
        issue.updatedAt("2025-02-13T08:04:58Z");
        LocalDateTime expectedTime = ZonedDateTime.parse(issue.updatedAt()).toLocalDateTime();

        when(responseSpec.body(Issue.class)).thenReturn(issue);

        LocalDateTime actualDateTime = githubClient.getIssueUpdate(URI.create("https://github.com/-1/-1/issues/-1"));

        assertThat(actualDateTime).isEqualTo(expectedTime);
    }

    @Test
    public void getIssuesFailTest() {

        when(responseSpec.body(Issue.class)).thenReturn(null);

        String url = "https://github.com/-1/-1/issues/-1";

        assertThatThrownBy(() -> githubClient.getIssueUpdate(URI.create(url))).isInstanceOf(BaseException.class);
        assertThatThrownBy(() -> githubClient.getIssueUpdate(URI.create(url)))
                .hasMessage("Ошибка при обращении по ссылке " + url);
    }

    @Test
    public void getRepositorySuccessTest() {
        GHRepository repository = new GHRepository();
        repository.updatedAt("2025-02-13T08:04:58Z");
        LocalDateTime expectedTime = ZonedDateTime.parse(repository.updatedAt()).toLocalDateTime();

        when(responseSpec.body(GHRepository.class)).thenReturn(repository);

        LocalDateTime actualDateTime = githubClient.getRepositoryUpdate(URI.create("https://github.com/-1/-1"));

        assertThat(actualDateTime).isEqualTo(expectedTime);
    }

    @Test
    public void getRepositoryFailTest() {

        when(responseSpec.body(GHRepository.class)).thenReturn(null);

        String url = "https://github.com/-1/-1";

        assertThatThrownBy(() -> githubClient.getRepositoryUpdate(URI.create(url)))
                .isInstanceOf(BaseException.class);
        assertThatThrownBy(() -> githubClient.getRepositoryUpdate(URI.create(url)))
                .hasMessage("Ошибка при обращении по ссылке " + url);
    }
}
