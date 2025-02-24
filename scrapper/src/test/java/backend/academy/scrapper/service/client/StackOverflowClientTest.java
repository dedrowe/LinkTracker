package backend.academy.scrapper.service.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.dto.stackOverflow.Question;
import backend.academy.scrapper.dto.stackOverflow.SOResponse;
import backend.academy.scrapper.service.apiClient.StackOverflowClient;
import backend.academy.shared.exceptions.ApiCallException;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

@ExtendWith(MockitoExtension.class)
public class StackOverflowClientTest {

    private final RestClient client = mock(RestClient.class);

    private final StackOverflowClient stackOverflowClient = new StackOverflowClient(client);

    private final RestClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);

    private final RestClient.RequestHeadersSpec requestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);

    private final RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

    @BeforeEach
    void setUp() {
        when(client.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri((Function<UriBuilder, URI>) any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    }

    @Test
    public void getQuestionsSuccessTest() {
        SOResponse response = new SOResponse();
        Question question = new Question();
        question.lastActivityDate(123123L);
        response.items(List.of(question));

        LocalDateTime expectedTime =
                Instant.ofEpochSecond(123123L).atZone(ZoneOffset.UTC).toLocalDateTime();

        when(responseSpec.body(SOResponse.class)).thenReturn(response);

        LocalDateTime actualTime =
                stackOverflowClient.getQuestionUpdate(URI.create("https://api.stackexchange.com/2.3/questions/-1"));

        assertThat(actualTime).isEqualTo(expectedTime);
    }

    @Test
    public void getQuestionsFailTest() {
        when(responseSpec.body(SOResponse.class)).thenReturn(null);

        String url = "https://api.stackexchange.com/2.3/questions/-1";

        assertThatThrownBy(() -> stackOverflowClient.getQuestionUpdate(URI.create(url)))
                .isInstanceOf(ApiCallException.class);
    }

    @Test
    public void getWrongQuestionUrlTest() {
        SOResponse response = new SOResponse();
        response.items(List.of());

        when(responseSpec.body(SOResponse.class)).thenReturn(response);

        String url = "https://api.stackexchange.com/2.3/questions/-1";

        assertThatThrownBy(() -> stackOverflowClient.getQuestionUpdate(URI.create(url)))
                .isInstanceOf(ApiCallException.class);
    }
}
