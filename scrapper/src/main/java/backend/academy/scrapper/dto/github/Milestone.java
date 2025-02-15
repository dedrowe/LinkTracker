package backend.academy.scrapper.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Milestone {
    @JsonProperty("id")
    private int id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("creator")
    private User creator;

    @JsonProperty("open_issues")
    private int openIssues;

    @JsonProperty("closed_issues")
    private int closedIssues;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    @JsonProperty("due_on")
    private String dueOn;

    @JsonProperty("closed_at")
    private String closedAt;
}
