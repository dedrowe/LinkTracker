package backend.academy.scrapper.dto.stackOverflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {

    @JsonProperty("account_id")
    private long accountId;

    @JsonProperty("reputation")
    private int reputation;

    @JsonProperty("user_id")
    private int userId;

    @JsonProperty("user_type")
    private String userType;

    @JsonProperty("accept_rate")
    private int acceptRate;

    @JsonProperty("profile_image")
    private String profileImage;

    @JsonProperty("display_name")
    private String displayName;

    @JsonProperty("link")
    private String link;
}
