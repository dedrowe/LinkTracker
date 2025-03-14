package backend.academy.scrapper.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LinkData {

    private Long id;

    private long linkId;

    private long chatId;

    private List<String> tags;

    private List<String> filters;

    private boolean deleted = false;

    public LinkData(long linkId, long chatId, List<String> tags, List<String> filters) {
        this.linkId = linkId;
        this.chatId = chatId;
        this.tags = tags;
        this.filters = filters;
    }

    public LinkData(Long id, long linkId, long chatId, List<String> tags, List<String> filters) {
        this.id = id;
        this.linkId = linkId;
        this.chatId = chatId;
        this.tags = tags;
        this.filters = filters;
    }
}
