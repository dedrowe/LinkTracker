package backend.academy.scrapper.dto.stackOverflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Question {

    @JsonProperty("tags")
    private List<String> tags;

    @JsonProperty("owner")
    private User owner;

    @JsonProperty("is_answered")
    private boolean isAnswered;

    @JsonProperty("view_count")
    private long viewCount;

    @JsonProperty("answers_count")
    private int answersCount;

    @JsonProperty("score")
    private int score;

    @JsonProperty("last_activity_date")
    private long lastActivityDate;

    @JsonProperty("creation_date")
    private long creationDate;

    @JsonProperty("question_id")
    private long questionId;

    @JsonProperty("content_license")
    private String contentLicense;

    @JsonProperty("link")
    private String link;

    @JsonProperty("title")
    private String title;
}
