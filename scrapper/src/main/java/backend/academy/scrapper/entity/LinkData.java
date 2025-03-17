package backend.academy.scrapper.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class LinkData {

    private Long id;

    private long linkId;

    private long chatId;

    private boolean deleted = false;

    public LinkData(long linkId, long chatId) {
        this.linkId = linkId;
        this.chatId = chatId;
    }

    public LinkData(Long id, long linkId, long chatId) {
        this.id = id;
        this.linkId = linkId;
        this.chatId = chatId;
    }
}
