package backend.academy.scrapper.service.client.wrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.dto.Update;
import backend.academy.scrapper.dto.github.Comment;
import backend.academy.scrapper.dto.github.Issue;
import backend.academy.scrapper.dto.github.PullRequest;
import backend.academy.scrapper.dto.github.User;
import backend.academy.scrapper.service.apiClient.GithubClient;
import backend.academy.scrapper.service.apiClient.wrapper.GithubWrapper;
import backend.academy.shared.exceptions.ApiCallException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GithubWrapperTest {

    private final GithubClient githubClient = Mockito.mock(GithubClient.class);

    private final GithubWrapper githubWrapper = new GithubWrapper(githubClient);

    private final String expectedUpdate = "2020-03-15T20:12:57Z";

    private final String lastUpdate = "2025-03-15T20:12:57Z";

    private final String expectedBody = "test body";

    private final String expectedTitle = "test title";

    private final User user = new User("test user", 0, "", "");

    private final PullRequest pr = new PullRequest(0, expectedTitle, user, expectedBody, lastUpdate);

    private final Issue issue = new Issue(expectedTitle, user, lastUpdate, lastUpdate, expectedBody);

    @Test
    public void getRepositoryUpdateWithPRTest() {
        URI uri = URI.create("https://github.com/-1/-1");
        Update expectedResult =
                new Update("\nПоследний PR:\n" + pr.getInfo(expectedBody.length()), Map.of("user", user.login()));
        when(githubClient.getIssues(any())).thenReturn(List.of());
        when(githubClient.getPullRequests(any())).thenReturn(List.of(pr));

        List<Update> actualResult = githubWrapper.getLastUpdate(
                uri, ZonedDateTime.parse(expectedUpdate).toLocalDateTime());

        assertThat(actualResult).containsExactly(expectedResult);
    }

    @Test
    public void getRepositoryUpdateWithIssueTest() {
        URI uri = URI.create("https://github.com/-1/-1");
        Update expectedResult =
                new Update("\nПоследний Issue:\n" + issue.getInfo(expectedBody.length()), Map.of("user", user.login()));
        when(githubClient.getIssues(any())).thenReturn(List.of(issue));
        when(githubClient.getPullRequests(any())).thenReturn(List.of());

        List<Update> actualResult = githubWrapper.getLastUpdate(
                uri, ZonedDateTime.parse(expectedUpdate).toLocalDateTime());

        assertThat(actualResult).containsExactly(expectedResult);
    }

    @Test
    public void getRepositoryUpdateWithPRAndIssueTest() {
        URI uri = URI.create("https://github.com/-1/-1");
        Update update1 =
                new Update("\nПоследний PR:\n" + pr.getInfo(expectedBody.length()), Map.of("user", user.login()));
        Update update2 =
                new Update("\nПоследний Issue:\n" + issue.getInfo(expectedBody.length()), Map.of("user", user.login()));
        when(githubClient.getIssues(any())).thenReturn(List.of(issue));
        when(githubClient.getPullRequests(any())).thenReturn(List.of(pr));

        List<Update> actualResult = githubWrapper.getLastUpdate(
                uri, ZonedDateTime.parse(expectedUpdate).toLocalDateTime());

        assertThat(actualResult).containsExactly(update1, update2);
    }

    @Test
    public void getIssueUpdateTest() {
        Comment comment = new Comment(0, user, lastUpdate, lastUpdate, expectedBody);
        URI uri = URI.create("https://github.com/-1/-1/issues/-1");
        Update update1 = new Update(
                "\nПоследний комментарий:\n" + comment.getInfo(expectedBody.length()), Map.of("user", user.login()));
        when(githubClient.getIssue(any())).thenReturn(issue);
        when(githubClient.getComments(any())).thenReturn(List.of(comment));

        List<Update> actualResult = githubWrapper.getLastUpdate(
                uri, ZonedDateTime.parse(expectedUpdate).toLocalDateTime());

        assertThat(actualResult).containsExactly(update1);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "https://github.com/-1/-1/issues",
                "https://github.com/-1/-1/issues/",
                "https://github.com/-1/-1/issues/1/1",
                "https://github.com/-1/",
                "https://github.com/-1",
            })
    @SuppressWarnings("JavaTimeDefaultTimeZone")
    public void getWrongUrlUpdateTest(String url) {
        URI uri = URI.create(url);

        assertThatThrownBy(() -> githubWrapper.getLastUpdate(uri, LocalDateTime.now()))
                .isInstanceOf(ApiCallException.class);
    }
}
