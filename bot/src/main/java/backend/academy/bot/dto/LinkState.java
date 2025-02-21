package backend.academy.bot.dto;

import backend.academy.bot.stateStorage.state.LinkTrackState;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class LinkState {

    private LinkTrackState state;

    private String link;

    private List<String> tags;

    private List<String> filters;
}
