package backend.academy.scrapper.service.client.wrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.dto.Update;
import backend.academy.scrapper.dto.stackOverflow.Answer;
import backend.academy.scrapper.dto.stackOverflow.Comment;
import backend.academy.scrapper.dto.stackOverflow.Question;
import backend.academy.scrapper.dto.stackOverflow.User;
import backend.academy.scrapper.service.apiClient.StackOverflowClient;
import backend.academy.scrapper.service.apiClient.wrapper.StackOverflowWrapper;
import backend.academy.shared.exceptions.ApiCallException;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StackOverflowWrapperTest {

    private final StackOverflowClient stackOverflowClient = mock(StackOverflowClient.class);

    private final StackOverflowWrapper stackOverflowWrapper = new StackOverflowWrapper(stackOverflowClient);

    private final long expectedUpdate = 123123L;

    private final long lastUpdate = 456789L;

    private final String expectedBody = "test body";

    private final String expectedTitle = "test title";

    private final User user = new User("test user");

    private final Comment comment = new Comment(user, lastUpdate, expectedBody);

    private final Answer answer = new Answer(user, lastUpdate, lastUpdate, List.of(), expectedBody);

    @Test
    public void getQuestionUpdateWithAnswerTest() {
        Question question = new Question(user, lastUpdate, lastUpdate, expectedTitle, List.of(), List.of(answer));
        when(stackOverflowClient.getQuestionUpdate(any())).thenReturn(question);
        URI uri = URI.create("https://stackoverflow.com/questions/-1");
        Update expectedResult = new Update(
                "\nПоследний ответ:\n" + "Тема вопроса: " + expectedTitle + "\n"
                        + answer.getInfo(expectedBody.length()),
                Map.of("user", user.displayName()));

        List<Update> actualResult = stackOverflowWrapper.getLastUpdate(
                uri,
                Instant.ofEpochSecond(expectedUpdate).atZone(ZoneOffset.UTC).toLocalDateTime());

        assertThat(actualResult).containsExactly(expectedResult);
    }

    @Test
    public void getQuestionUpdateWithCommentTest() {
        Question question = new Question(user, lastUpdate, lastUpdate, expectedTitle, List.of(comment), List.of());
        when(stackOverflowClient.getQuestionUpdate(any())).thenReturn(question);
        URI uri = URI.create("https://stackoverflow.com/questions/-1");
        Update expectedResult = new Update(
                "\nПоследний комментарий:\n" + "Тема вопроса: " + expectedTitle + "\n"
                        + comment.getInfo(expectedBody.length()),
                Map.of("user", user.displayName()));

        List<Update> actualResult = stackOverflowWrapper.getLastUpdate(
                uri,
                Instant.ofEpochSecond(expectedUpdate).atZone(ZoneOffset.UTC).toLocalDateTime());

        assertThat(actualResult).containsExactly(expectedResult);
    }

    @Test
    public void getQuestionUpdateWithCommentAndAnswerTest() {
        String expectedAnswerBody = "test answer body";
        String expectedCommentBody = "test comment body";
        String expectedTitle = "test title";
        Comment answerComment = new Comment(user, lastUpdate + 2, expectedCommentBody);
        Answer answer = new Answer(user, lastUpdate, lastUpdate, List.of(answerComment), expectedAnswerBody);
        Question question =
                new Question(user, lastUpdate, lastUpdate, expectedTitle, List.of(comment), List.of(answer));
        when(stackOverflowClient.getQuestionUpdate(any())).thenReturn(question);
        URI uri = URI.create("https://stackoverflow.com/questions/-1");
        Update update1 = new Update(
                "\nПоследний ответ:\n" + "Тема вопроса: " + expectedTitle + "\n"
                        + answer.getInfo(expectedAnswerBody.length()),
                Map.of("user", user.displayName()));
        Update update2 = new Update(
                "\nПоследний комментарий:\n" + "Тема вопроса: " + expectedTitle + "\n"
                        + answerComment.getInfo(expectedCommentBody.length()),
                Map.of("user", user.displayName()));

        List<Update> actualResult = stackOverflowWrapper.getLastUpdate(
                uri,
                Instant.ofEpochSecond(expectedUpdate).atZone(ZoneOffset.UTC).toLocalDateTime());

        assertThat(actualResult).containsExactly(update1, update2);
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

        assertThatThrownBy(() -> stackOverflowWrapper.getLastUpdate(uri, LocalDateTime.now()))
                .isInstanceOf(ApiCallException.class);
    }
}
