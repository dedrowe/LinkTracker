package backend.academy.scrapper.service.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import backend.academy.scrapper.dto.github.Comment;
import backend.academy.scrapper.dto.github.GHRepository;
import backend.academy.scrapper.dto.github.Issue;
import backend.academy.scrapper.dto.github.PullRequest;
import backend.academy.scrapper.service.apiClient.GithubClient;
import backend.academy.shared.exceptions.ApiCallException;
import backend.academy.shared.utils.client.RetryWrapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

public class GithubClientTest {

    private final String mockServerAddress = "localhost";

    private final int mockServerPort = 8080;

    private final WireMockServer wireMockServer = new WireMockServer(
            WireMockConfiguration.options().bindAddress(mockServerAddress).port(mockServerPort));

    private final GithubClient githubClient = new GithubClient(
            RestClient.create("http://" + mockServerAddress + ":" + mockServerPort), new RetryWrapper());

    @BeforeEach
    void setUp() {
        wireMockServer.start();
        configureFor(mockServerAddress, wireMockServer.port());
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    private final String issueUrl = "https://github.com/-1/-1/issues/-1";

    private final String issuesUrl = "https://github.com/-1/-1/issues";

    private final String commentsUrl = "https://github.com/-1/-1/issues/-1/comments";

    private final String repositoriesUrl = "https://github.com/-1/-1";

    private final String pullsUrl = "https://github.com/-1/-1/pulls";

    @Test
    public void getIssueSuccessTest() {
        String expectedUpdate = "2025-02-13T08:04:58Z";
        String expectedBody = "test";
        String wireMockUrl = "/-1/-1/issues/-1";
        stubFor(get(urlPathMatching(wireMockUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("{\"body\": \"" + expectedBody + "\"," + "\"created_at\": \""
                                + expectedUpdate + "\"," + "\"updated_at\": \""
                                + expectedUpdate + "\"}")));
        Issue expectedResult = new Issue(null, null, expectedUpdate, expectedUpdate, expectedBody);

        Issue actualResult = githubClient.getIssue(URI.create(issueUrl));

        verify(getRequestedFor(urlPathMatching(wireMockUrl)));
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    public void getIssueFailTest() {
        String expectedDescription = "Ошибка при обращении по ссылке";
        int status = 400;
        stubFor(get(urlPathMatching("/-1/-1/issues/-1")).willReturn(aResponse().withStatus(200)));

        assertThatExceptionOfType(ApiCallException.class)
                .isThrownBy(() -> githubClient.getIssue(URI.create(issueUrl)))
                .satisfies(ex -> {
                    assertThat(ex.description()).isEqualTo(expectedDescription);
                    assertThat(ex.getMessage()).isEqualTo(expectedDescription);
                    assertThat(ex.url()).isEqualTo(issueUrl);
                    assertThat(ex.code()).isEqualTo(status);
                });
    }

    @Test
    public void getIssuesSuccessTest() {
        String wireMockUrl = "/-1/-1/issues";
        String expectedBody1 = "test1";
        String expectedBody2 = "test2";
        Issue issue1 = new Issue(null, null, null, null, expectedBody1);
        Issue issue2 = new Issue(null, null, null, null, expectedBody2);
        stubFor(get(urlPathMatching(wireMockUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("[{\"body\": \"" + expectedBody1 + "\"}," + "{\"body\": \"" + expectedBody2 + "\"}"
                                + "]")));

        List<Issue> actualResult = githubClient.getIssues(URI.create(issuesUrl));

        assertThat(actualResult).containsExactly(issue1, issue2);
    }

    @Test
    public void getIssuesFailTest() {
        String expectedDescription = "Ошибка при обращении по ссылке";
        int status = 400;
        stubFor(get(urlPathMatching("/-1/-1/issues")).willReturn(aResponse().withStatus(200)));

        assertThatExceptionOfType(ApiCallException.class)
                .isThrownBy(() -> githubClient.getIssues(URI.create(issuesUrl)))
                .satisfies(ex -> {
                    assertThat(ex.description()).isEqualTo(expectedDescription);
                    assertThat(ex.getMessage()).isEqualTo(expectedDescription);
                    assertThat(ex.url()).isEqualTo(issuesUrl);
                    assertThat(ex.code()).isEqualTo(status);
                });
    }

    @Test
    public void getCommentsSuccessTest() {
        String wireMockUrl = "/-1/-1/issues/-1/comments";
        String expectedBody1 = "test1";
        String expectedBody2 = "test2";
        Comment comment1 = new Comment(0, null, null, null, expectedBody1);
        Comment comment2 = new Comment(0, null, null, null, expectedBody2);
        stubFor(get(urlPathMatching(wireMockUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("[{\"body\": \"" + expectedBody1 + "\"}," + "{\"body\": \"" + expectedBody2 + "\"}"
                                + "]")));

        List<Comment> actualResult = githubClient.getComments(URI.create(commentsUrl));

        assertThat(actualResult).containsExactly(comment1, comment2);
    }

    @Test
    public void getCommentsFailTest() {
        String expectedDescription = "Ошибка при обращении по ссылке";
        int status = 400;
        stubFor(get(urlPathMatching("/-1/-1/issues/-1/comments"))
                .willReturn(aResponse().withStatus(200)));

        assertThatExceptionOfType(ApiCallException.class)
                .isThrownBy(() -> githubClient.getComments(URI.create(commentsUrl)))
                .satisfies(ex -> {
                    assertThat(ex.description()).isEqualTo(expectedDescription);
                    assertThat(ex.getMessage()).isEqualTo(expectedDescription);
                    assertThat(ex.url()).isEqualTo(commentsUrl);
                    assertThat(ex.code()).isEqualTo(status);
                });
    }

    @Test
    public void getRepositorySuccessTest() {
        String expectedUpdate = "2025-02-13T08:04:58Z";
        String expectedName = "test name";
        String expectedDescription = "test description";
        String wireMockUrl = "/-1/-1";
        stubFor(get(urlPathMatching(wireMockUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("{\"updated_at\": \"" + expectedUpdate + "\"," + "\"name\": \""
                                + expectedName + "\"," + "\"full_name\": \""
                                + expectedName + "\"," + "\"description\": \""
                                + expectedDescription + "\"," + "\"created_at\": \""
                                + expectedUpdate + "\"" + "}")));
        GHRepository expectedResult = new GHRepository(
                expectedName, expectedName, null, expectedDescription, expectedUpdate, expectedUpdate, null);

        GHRepository actualResult = githubClient.getRepository(URI.create(repositoriesUrl));

        verify(getRequestedFor(urlPathMatching(wireMockUrl)));
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    public void getRepositoryFailTest() {
        String expectedDescription = "Ошибка при обращении по ссылке";
        int status = 400;
        stubFor(get(urlPathMatching("/-1/-1")).willReturn(aResponse().withStatus(200)));

        assertThatExceptionOfType(ApiCallException.class)
                .isThrownBy(() -> githubClient.getRepository(URI.create(repositoriesUrl)))
                .satisfies(ex -> {
                    assertThat(ex.description()).isEqualTo(expectedDescription);
                    assertThat(ex.getMessage()).isEqualTo(expectedDescription);
                    assertThat(ex.url()).isEqualTo(repositoriesUrl);
                    assertThat(ex.code()).isEqualTo(status);
                });
    }

    @Test
    public void getPullRequestsSuccessTest() {
        String wireMockUrl = "/-1/-1/pulls";
        String expectedBody1 = "test1";
        String expectedBody2 = "test2";
        PullRequest pull1 = new PullRequest(0, null, null, expectedBody1, null);
        PullRequest pull2 = new PullRequest(0, null, null, expectedBody2, null);
        stubFor(get(urlPathMatching(wireMockUrl))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("[{\"body\": \"" + expectedBody1 + "\"}," + "{\"body\": \"" + expectedBody2 + "\"}"
                                + "]")));

        List<PullRequest> actualResult = githubClient.getPullRequests(URI.create(pullsUrl));

        assertThat(actualResult).containsExactly(pull1, pull2);
    }

    @Test
    public void getPullRequestsFailTest() {
        String expectedDescription = "Ошибка при обращении по ссылке";
        int status = 400;
        stubFor(get(urlPathMatching("/-1/-1/pulls")).willReturn(aResponse().withStatus(200)));

        assertThatExceptionOfType(ApiCallException.class)
                .isThrownBy(() -> githubClient.getPullRequests(URI.create(pullsUrl)))
                .satisfies(ex -> {
                    assertThat(ex.description()).isEqualTo(expectedDescription);
                    assertThat(ex.getMessage()).isEqualTo(expectedDescription);
                    assertThat(ex.url()).isEqualTo(pullsUrl);
                    assertThat(ex.code()).isEqualTo(status);
                });
    }

    @Test
    public void responseCode400Test() {
        String expectedDescription = "Ошибка при обращении по ссылке";
        String expectedMessage = "example message";
        int expectedStatusCode = 400;
        String expectedUrl = "https://example.com";
        stubFor(get(urlPathMatching(".*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusCode)
                        .withBody(expectedMessage)));

        assertThatExceptionOfType(ApiCallException.class)
                .isThrownBy(() -> githubClient.getRepository(URI.create(expectedUrl)))
                .satisfies(ex -> {
                    assertThat(ex.description()).isEqualTo(expectedDescription);
                    assertThat(ex.getMessage()).isEqualTo(expectedMessage);
                    assertThat(ex.url()).isEqualTo(expectedUrl);
                    assertThat(ex.code()).isEqualTo(expectedStatusCode);
                });
    }

    @Test
    public void responseCode500Test() {
        String expectedDescription = "Сервис сейчас недоступен";
        int expectedStatusCode = 500;
        String expectedUrl = "https://example.com";
        stubFor(get(urlPathMatching(".*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(expectedStatusCode)));

        assertThatExceptionOfType(ApiCallException.class)
                .isThrownBy(() -> githubClient.getRepository(URI.create(expectedUrl)))
                .satisfies(ex -> {
                    assertThat(ex.description()).isEqualTo(expectedDescription);
                    assertThat(ex.getMessage()).isEqualTo(expectedDescription);
                    assertThat(ex.url()).isEqualTo(expectedUrl);
                    assertThat(ex.code()).isEqualTo(expectedStatusCode);
                });
    }
}
