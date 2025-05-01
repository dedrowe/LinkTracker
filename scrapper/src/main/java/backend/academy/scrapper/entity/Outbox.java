package backend.academy.scrapper.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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

    @Column(name = "send_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime sendTime;

    public Outbox(long linkId, String link, long chatId, String description, LocalDateTime sendTime) {
        this.linkId = linkId;
        this.link = link;
        this.chatId = chatId;
        this.description = description;
        this.sendTime = sendTime;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Outbox outbox)) return false;
        return linkId == outbox.linkId
                && chatId == outbox.chatId
                && Objects.equals(link, outbox.link)
                && Objects.equals(description, outbox.description)
                && sendTime.toEpochSecond(ZoneOffset.UTC) == outbox.sendTime.toEpochSecond(ZoneOffset.UTC);
    }

    @Override
    public int hashCode() {
        return Objects.hash(linkId, link, chatId, description, sendTime);
    }
}
