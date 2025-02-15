package backend.academy.scrapper.dto.stackOverflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SOResponse {

    @JsonProperty("items")
    private List<Question> items;

    @JsonProperty("has_more")
    private boolean hasMore;

    @JsonProperty("quota_max")
    private int quotaMax;

    @JsonProperty("quota_remaining")
    private int quotaRemaining;
}
