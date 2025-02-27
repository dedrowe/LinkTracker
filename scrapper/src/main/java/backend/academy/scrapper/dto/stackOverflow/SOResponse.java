package backend.academy.scrapper.dto.stackOverflow;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SOResponse(List<Question> items, boolean hasMore, int quotaMax, int quotaRemaining) {}
