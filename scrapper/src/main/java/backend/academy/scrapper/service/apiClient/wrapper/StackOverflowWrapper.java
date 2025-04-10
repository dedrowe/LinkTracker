package backend.academy.scrapper.service.apiClient.wrapper;

import backend.academy.scrapper.dto.stackOverflow.Answer;
import backend.academy.scrapper.dto.stackOverflow.Comment;
import backend.academy.scrapper.dto.stackOverflow.Question;
import backend.academy.scrapper.service.apiClient.StackOverflowClient;
import backend.academy.shared.exceptions.ApiCallException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component("stackoverflow.com")
@Slf4j
@RequiredArgsConstructor
public class StackOverflowWrapper implements ApiClientWrapper {

    private static final int BODY_PREVIEW_LENGTH = 200;

    private final StackOverflowClient client;

    @Override
    public Optional<String> getLastUpdate(URI uri, LocalDateTime lastUpdate) {
        String[] path = uri.getPath().split("/");
        if ((path.length == 3 || path.length == 4) && path[1].equals("questions")) {
            return getQuestionUpdate(uri, lastUpdate);
        } else {
            throw new ApiCallException("Ресурс не поддерживается", 400, uri.toString());
        }
    }

    private Optional<String> getQuestionUpdate(URI uri, LocalDateTime lastUpdate) {
        Question question = client.getQuestionUpdate(uri);

        long updatedAt = lastUpdate.toEpochSecond(ZoneOffset.UTC);

        Answer lastAnswer = question.answers().stream()
                .max(Comparator.comparing(Answer::lastActivityDate))
                .orElse(null);
        StringBuilder sb = new StringBuilder();
        if (lastAnswer != null && lastAnswer.lastActivityDate() > updatedAt) {
            sb.append("\nПоследний ответ:\n");
            sb.append("Тема вопроса: ").append(question.title()).append("\n");
            sb.append(lastAnswer.getInfo(BODY_PREVIEW_LENGTH));
        }
        Comment lastComment = Stream.concat(
                        question.comments().stream(),
                        question.answers().stream().flatMap(answer -> answer.comments().stream()))
                .max(Comparator.comparing(Comment::creationDate))
                .orElse(null);
        if (lastComment != null && lastComment.creationDate() > updatedAt) {
            sb.append("\nПоследний комментарий:\n");
            sb.append("Тема вопроса: ").append(question.title()).append("\n");
            sb.append(lastComment.getInfo(BODY_PREVIEW_LENGTH));
        }
        return sb.isEmpty() ? Optional.empty() : Optional.of(sb.toString());
    }

    @Override
    public void checkResource(URI uri) {
        String[] path = uri.getPath().split("/");
        if ((path.length == 3 || path.length == 4) && path[1].equals("questions")) {
            client.getQuestionUpdate(uri);
        } else {
            throw new ApiCallException("Ресурс не поддерживается", 400, uri.toString());
        }
    }
}
