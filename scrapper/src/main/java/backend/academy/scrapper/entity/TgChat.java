package backend.academy.scrapper.entity;

import backend.academy.scrapper.entity.jpa.JpaLinkData;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NaturalId;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tg_chats")
public class TgChat {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tg_chats_id_gen")
    @SequenceGenerator(name = "tg_chats_id_gen", allocationSize = 1, sequenceName = "tg_chats_id_seq")
    private Long id;

    @NaturalId
    @Column(name = "chat_id", nullable = false, unique = true)
    private long chatId;

    @Column(name = "deleted")
    private boolean deleted = false;

    @OneToMany(mappedBy = "tgChat")
    private List<JpaLinkData> linksData;

    public TgChat(long chatId) {
        this.chatId = chatId;
    }

    public TgChat(Long id, long chatId) {
        this.id = id;
        this.chatId = chatId;
    }

    public TgChat(Long id, long chatId, boolean deleted) {
        this.id = id;
        this.chatId = chatId;
        this.deleted = deleted;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TgChat tgChat = (TgChat) o;
        return chatId == tgChat.chatId && deleted == tgChat.deleted;
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatId, deleted);
    }
}
