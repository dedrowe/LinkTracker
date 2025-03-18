package backend.academy.scrapper.entity;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
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
@Table(name = "links")
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
    generator = "links_id_gen")
    @SequenceGenerator(name = "links_id_gen",
    allocationSize = 1,
    sequenceName = "links_id_seq")
    private Long id;

    @Column(name = "link", nullable = false, unique = true)
    private String link;

    @Column(name = "last_update", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime lastUpdate;

    @Column(name = "deleted")
    private boolean deleted = false;

    public Link(String link) {
        this.link = link;
        this.lastUpdate = LocalDateTime.now(ZoneOffset.UTC);
    }

    public Link(Long id, String link, LocalDateTime lastUpdate) {
        this.id = id;
        this.link = link;
        this.lastUpdate = lastUpdate;
    }
}
