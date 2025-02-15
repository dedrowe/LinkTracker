package backend.academy.scrapper.service.client.wrapper;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import backend.academy.scrapper.exceptionHandling.exceptions.ScrapperBaseException;
import backend.academy.scrapper.service.apiClient.StackOverflowClient;
import backend.academy.scrapper.service.apiClient.wrapper.StackOverflowWrapper;
import java.net.URI;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StackOverflowWrapperTest {

    StackOverflowClient stackOverflowClient = mock(StackOverflowClient.class);

    StackOverflowWrapper stackOverflowWrapper = new StackOverflowWrapper(stackOverflowClient);

    @ParameterizedTest
    @ValueSource(
            strings = {
                "https://api.stackexchange.com/2.3/questions/-1",
                "https://api.stackexchange.com/2.3/questions/-1/",
                "https://api.stackexchange.com/2.3/questions/123123"
            })
    public void getQuestionUpdateTest(String url) {
        URI uri = URI.create(url);

        stackOverflowWrapper.getLastUpdate(uri);

        verify(stackOverflowClient, times(1)).getQuestionUpdate(any());
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "https://api.stackexchange.com/2.3/questions/-1/-1",
                "https://api.stackexchange.com/2.3/questions/",
                "https://api.stackexchange.com/2.3"
            })
    public void getWrongUrlUpdateTest(String url) {
        URI uri = URI.create(url);

        assertThatThrownBy(() -> stackOverflowWrapper.getLastUpdate(uri)).isInstanceOf(ScrapperBaseException.class);
        assertThatThrownBy(() -> stackOverflowWrapper.getLastUpdate(uri)).hasMessage("Ресурс не поддерживается " + uri);
    }
}
