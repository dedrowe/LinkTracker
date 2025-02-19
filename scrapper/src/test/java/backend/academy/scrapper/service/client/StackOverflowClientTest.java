package backend.academy.scrapper.service.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.dto.stackOverflow.Question;
import backend.academy.scrapper.dto.stackOverflow.SOResponse;
import backend.academy.scrapper.service.apiClient.StackOverflowClient;
import backend.academy.shared.exceptions.BaseException;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
public class StackOverflowClientTest {

    private final RestClient client = mock(RestClient.class);

    private final StackOverflowClient stackOverflowClient = new StackOverflowClient(client);

    private final RestClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);

    private final RestClient.RequestHeadersSpec requestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);

    private final RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

    @Test
    public void getQuestionsSuccessTest() {
        SOResponse response = new SOResponse();
        Question question = new Question();
        question.lastActivityDate(123123L);
        response.items(List.of(question));

        LocalDateTime expectedTime =
                Instant.ofEpochMilli(123123L).atZone(ZoneId.systemDefault()).toLocalDateTime();

        when(client.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.body(SOResponse.class)).thenReturn(response);

        LocalDateTime actualTime =
                stackOverflowClient.getQuestionUpdate(URI.create("https://api.stackexchange.com/2.3/questions/-1"));

        assertThat(actualTime).isEqualTo(expectedTime);
    }

    @Test
    public void getQuestionsFailTest() {
        when(client.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.body(SOResponse.class)).thenReturn(null);

        String url = "https://api.stackexchange.com/2.3/questions/-1";

        assertThatThrownBy(() -> stackOverflowClient.getQuestionUpdate(URI.create(url)))
                .isInstanceOf(BaseException.class);
        assertThatThrownBy(() -> stackOverflowClient.getQuestionUpdate(URI.create(url)))
                .hasMessage("Ошибка при обращении по ссылке " + url);
    }

    @Test
    public void getWrongQuestionUrlTest() {
        SOResponse response = new SOResponse();
        response.items(List.of());

        when(client.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.body(SOResponse.class)).thenReturn(response);

        String url = "https://api.stackexchange.com/2.3/questions/-1";

        assertThatThrownBy(() -> stackOverflowClient.getQuestionUpdate(URI.create(url)))
                .isInstanceOf(BaseException.class);
        assertThatThrownBy(() -> stackOverflowClient.getQuestionUpdate(URI.create(url)))
                .hasMessage("Ошибка при обращении по ссылке " + url);
    }
}
