package backend.academy.scrapper.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import backend.academy.scrapper.service.apiClient.wrapper.GithubWrapper;
import backend.academy.scrapper.service.apiClient.wrapper.StackOverflowWrapper;
import backend.academy.shared.exceptions.BaseException;
import java.net.URI;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class LinkDispatcherTest {

    private final LinkDispatcher dispatcher = new LinkDispatcher(Map.of(
            "github.com", mock(GithubWrapper.class),
            "stackoverflow.com", mock(StackOverflowWrapper.class)));

    @Test
    public void githubTest() {
        URI uri = URI.create("https://github.com/asd/asd/issues/1");

        assertThat(dispatcher.dispatchLink(uri)).isInstanceOf(GithubWrapper.class);
    }

    @Test
    public void stackOverflowTest() {
        URI uri = URI.create("https://stackoverflow.com/questions/-1");

        assertThat(dispatcher.dispatchLink(uri)).isInstanceOf(StackOverflowWrapper.class);
    }

    @Test
    public void wrongServiceTest() {
        URI uri = URI.create("https://google.com/");

        assertThatThrownBy(() -> dispatcher.dispatchLink(uri)).isInstanceOf(BaseException.class);
        assertThatThrownBy(() -> dispatcher.dispatchLink(uri))
                .hasMessage("Отслеживание ссылок этого сервиса не поддерживается");
    }
}
