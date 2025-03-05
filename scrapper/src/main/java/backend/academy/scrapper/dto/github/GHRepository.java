package backend.academy.scrapper.dto.github;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record GHRepository(
        long id,
        String name,
        String fullName,
        User owner,
        boolean isPrivate,
        String htmlUrl,
        String description,
        boolean fork,
        String createdAt,
        String updatedAt,
        String pushedAt,
        int stargazersCount,
        int watchersCount,
        String language,
        int forksCount,
        int openIssuesCount,
        String defaultBranch) {}
