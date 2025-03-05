package backend.academy.scrapper.dto.stackOverflow;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record Question(
        List<String> tags,
        User owner,
        boolean isAnswered,
        long viewCount,
        int answersCount,
        int score,
        long lastActivityDate,
        long creationDate,
        long questionId,
        String contentLicense,
        String link,
        String title) {}
