package backend.academy.scrapper.dto.github;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PullRequest(long id, String title, User user, String body, String createdAt) {

    public String getInfo(int maxBodyLength) {
        String body = body() == null ? "" : body();
        if (body.length() > maxBodyLength) {
            body = body.substring(0, maxBodyLength) + "...";
        }
        return "Название: " + this.title() + "\n" + "Создатель: "
                + this.user().login() + "\n" + "Создан: "
                + this.createdAt() + "\n" + "Описание: "
                + body + "\n";
    }
}
