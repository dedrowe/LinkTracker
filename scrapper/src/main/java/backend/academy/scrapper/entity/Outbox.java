package backend.academy.scrapper.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "outbox")
public class Outbox {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "outbox_id_gen")
    @SequenceGenerator(name = "outbox_id_gen", allocationSize = 1, sequenceName = "outbox_id_seq")
    private Long id;

    @Column(name = "link_id", nullable = false)
    private long linkId;

    @Column(name = "link", nullable = false)
    private String link;

    @Column(name = "chat_id", nullable = false)
    private long chatId;

    @Column(name = "description", nullable = false)
    private String description;

    public Outbox(long linkId, String link, long chatId, String description) {
        this.linkId = linkId;
        this.link = link;
        this.chatId = chatId;
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Outbox outbox = (Outbox) o;
        return linkId == outbox.linkId
                && chatId == outbox.chatId
                && Objects.equals(link, outbox.link)
                && Objects.equals(description, outbox.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(linkId, link, chatId, description);
    }
}
