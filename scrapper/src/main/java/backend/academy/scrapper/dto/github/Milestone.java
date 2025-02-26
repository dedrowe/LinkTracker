package backend.academy.scrapper.dto.github;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record Milestone(
    int id,
    String title,
    String description,
    User creator,
    int openIssues,
    int closedIssues,
    String createdAt,
    String updatedAt,
    String dueOn,
    String closedAt
) {
}
