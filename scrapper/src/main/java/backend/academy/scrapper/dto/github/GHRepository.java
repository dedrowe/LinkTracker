package backend.academy.scrapper.dto.github;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record GHRepository(
        String name,
        String fullName,
        User owner,
        String description,
        String createdAt,
        String updatedAt,
        String pushedAt) {}
