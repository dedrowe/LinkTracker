package backend.academy.scrapper.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Issue {
    @JsonProperty("id")
    private long id;

    @JsonProperty("node_id")
    private String nodeId;

    @JsonProperty("url")
    private String url;

    @JsonProperty("number")
    private int number;

    @JsonProperty("title")
    private String title;

    @JsonProperty("user")
    private User user;

    @JsonProperty("state")
    private String state;

    @JsonProperty("locked")
    private boolean locked;

    @JsonProperty("labels")
    private List<Label> labels;

    @JsonProperty("assignee")
    private User assignee;

    @JsonProperty("assignees")
    private List<User> assignees;

    @JsonProperty("milestone")
    private Milestone milestone;

    @JsonProperty("comments")
    private int comments;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    @JsonProperty("closed_at")
    private String closedAt;

    @JsonProperty("body")
    private String body;
}
