package backend.academy.scrapper.service.client.wrapper;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import backend.academy.scrapper.service.apiClient.StackOverflowClient;
import backend.academy.scrapper.service.apiClient.wrapper.StackOverflowWrapper;
import backend.academy.shared.exceptions.ApiCallException;
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
                "https://stackoverflow.com/questions/-1",
                "https://stackoverflow.com/questions/-1/",
                "https://stackoverflow.com/questions/123123"
            })
    public void getQuestionUpdateTest(String url) {
        URI uri = URI.create(url);

        stackOverflowWrapper.getLastUpdate(uri);

        verify(stackOverflowClient, times(1)).getQuestionUpdate(any());
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "https://stackoverflow.com/questions/-1/-1/-1",
                "https://stackoverflow.com/questions/",
                "https://stackoverflow.com"
            })
    public void getWrongUrlUpdateTest(String url) {
        URI uri = URI.create(url);

        assertThatThrownBy(() -> stackOverflowWrapper.getLastUpdate(uri)).isInstanceOf(ApiCallException.class);
    }
}
