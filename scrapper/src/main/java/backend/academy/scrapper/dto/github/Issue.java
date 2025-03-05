package backend.academy.scrapper.dto.github;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record Issue(
        long id,
        String nodeId,
        String url,
        int number,
        String title,
        User user,
        String state,
        boolean locked,
        List<Label> labels,
        User assignee,
        List<User> assignees,
        Milestone milestone,
        int comments,
        String createdAt,
        String updatedAt,
        String closedAt,
        String body) {}
