package backend.academy.scrapper.dto.github;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Map;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record Comment(long id, User user, String createdAt, String updatedAt, String body) {

    public String getInfo(int maxBodyLength) {
        String body = body() == null ? "" : body();
        if (body.length() > maxBodyLength) {
            body = body.substring(0, maxBodyLength) + "...";
        }
        return "Автор: " + this.user().login() + "\n" + "Текст: " + body + "\n";
    }

    public Map<String, String> getPossibleFilters() {
        return Map.of("user", user().login());
    }
}
