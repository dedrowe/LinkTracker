package backend.academy.scrapper.entity.jdbc;

import backend.academy.scrapper.entity.LinkData;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        JdbcLinkData that = (JdbcLinkData) o;
        return deleted == that.deleted && Objects.equals(linkId, that.linkId) && Objects.equals(chatId, that.chatId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(linkId, chatId, deleted);
    }
}
