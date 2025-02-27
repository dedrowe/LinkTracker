package backend.academy.scrapper.dto.stackOverflow;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record User(
        long accountId,
        int reputation,
        int userId,
        String userType,
        int acceptRate,
        String profileImage,
        String displayName,
        String link) {}
