package backend.academy.scrapper.entity.jdbc;

import backend.academy.scrapper.entity.LinkData;
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
public class JdbcLinkData implements LinkData {

    private Long id;

    private Long linkId;

    private Long chatId;

    private boolean deleted = false;

    @Override
    public Long id() {
        return id;
    }

    @Override
    public void id(Long id) {
        this.id = id;
    }

    @Override
    public Long linkId() {
        return linkId;
    }

    @Override
    public Long chatId() {
        return chatId;
    }

    @Override
    public boolean deleted() {
        return deleted;
    }

    @Override
    public void deleted(boolean deleted) {
        this.deleted = deleted;
    }

    public JdbcLinkData(long linkId, long chatId) {
        this.linkId = linkId;
        this.chatId = chatId;
    }

    public JdbcLinkData(Long id, long linkId, long chatId) {
        this.id = id;
        this.linkId = linkId;
        this.chatId = chatId;
    }

    public JdbcLinkData(long linkId, long chatId, boolean deleted) {
        this.linkId = linkId;
        this.chatId = chatId;
        this.deleted = deleted;
    }
}
