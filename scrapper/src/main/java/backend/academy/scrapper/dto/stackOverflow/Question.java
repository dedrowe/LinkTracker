package backend.academy.scrapper.dto.stackOverflow;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record Question(
        User owner,
        long lastActivityDate,
        long creationDate,
        String title,
        @JsonSetter(nulls = Nulls.AS_EMPTY) List<Comment> comments,
        @JsonSetter(nulls = Nulls.AS_EMPTY) List<Answer> answers) {}
