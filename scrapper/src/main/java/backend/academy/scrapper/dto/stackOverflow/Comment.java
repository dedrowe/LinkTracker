package backend.academy.scrapper.dto.stackOverflow;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.Instant;
import java.time.ZoneOffset;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record Comment(User owner, long creationDate, String body) {

    public String getInfo(int maxBodyLength) {
        String body = body() == null ? "" : body();
        if (body.length() > maxBodyLength) {
            body = body.substring(0, maxBodyLength) + "...";
        }
        return "Автор: " + this.owner().displayName() + "\n" + "Создан: "
                + Instant.ofEpochSecond(creationDate()).atZone(ZoneOffset.UTC).toLocalDateTime() + "\n" + "Описание: "
                + body + "\n";
    }
}
