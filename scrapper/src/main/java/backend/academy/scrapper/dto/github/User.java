package backend.academy.scrapper.dto.github;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record User(
    String login,
    int id,
    String avatarUrl,
    String htmlUrl
) {
}
