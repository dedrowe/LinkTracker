package backend.academy.scrapper.dto.stackOverflow;

import backend.academy.scrapper.utils.UtcDateTimeProvider;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Map;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record Comment(User owner, long creationDate, String body) {

    public String getInfo(int maxBodyLength) {
        String body = body() == null ? "" : body();
        if (body.length() > maxBodyLength) {
            body = body.substring(0, maxBodyLength) + "...";
        }
        return "Автор: " + this.owner().displayName() + "\n" + "Создан: "
                + UtcDateTimeProvider.of(creationDate()) + "\n" + "Описание: " + body + "\n";
    }

    public Map<String, String> getPossibleFilters() {
        return Map.of("user", owner().displayName());
    }
}
