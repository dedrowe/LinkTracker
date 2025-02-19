package backend.academy.scrapper.service.client.wrapper;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.shared.exceptions.BaseException;
import backend.academy.scrapper.service.apiClient.GithubClient;
import backend.academy.scrapper.service.apiClient.wrapper.GithubWrapper;
import java.net.URI;
import java.time.LocalDateTime;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GithubWrapperTest {

    private final GithubClient githubClient = Mockito.mock(GithubClient.class);

    private final GithubWrapper githubWrapper = new GithubWrapper(githubClient);

    @ParameterizedTest
    @ValueSource(strings = {"https://github.com/-1/-1", "https://github.com/asdasd/asdasd/"})
    public void getRepositoryUpdateTest(String url) {
        when(githubClient.getRepositoryUpdate(any())).thenReturn(LocalDateTime.now());

        githubWrapper.getLastUpdate(URI.create(url));

        verify(githubClient, times(1)).getRepositoryUpdate(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://github.com/-1/-1/issues/-1", "https://github.com/asdasd/asdasd/issues/1/"})
    public void getIssueUpdateTest(String url) {
        when(githubClient.getIssueUpdate(any())).thenReturn(LocalDateTime.now());

        githubWrapper.getLastUpdate(URI.create(url));

        verify(githubClient, times(1)).getIssueUpdate(any());
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
    public void getWrongUrlUpdateTest(String url) {
        URI uri = URI.create(url);

        assertThatThrownBy(() -> githubWrapper.getLastUpdate(uri)).isInstanceOf(BaseException.class);
        assertThatThrownBy(() -> githubWrapper.getLastUpdate(uri)).hasMessage("Ресурс не поддерживается " + uri);
    }
}
