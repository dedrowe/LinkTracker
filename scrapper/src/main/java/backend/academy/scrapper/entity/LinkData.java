package backend.academy.scrapper.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
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
@Entity
@Table(name = "links_data")
public class LinkData {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
        generator = "links_data_id_gen")
    @SequenceGenerator(name = "links_data_id_gen",
        allocationSize = 1,
        sequenceName = "links_data_id_seq")
    private Long id;

    @Column(name = "link_id", nullable = false)
    private long linkId;

    @Column(name = "chat_id", nullable = false)
    private long chatId;

    @Column(name = "deleted")
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
