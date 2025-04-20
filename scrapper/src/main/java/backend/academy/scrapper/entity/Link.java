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
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
@Table(name = "links")
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "links_id_gen")
    @SequenceGenerator(name = "links_id_gen", allocationSize = 1, sequenceName = "links_id_seq")
    private Long id;

    @NaturalId
    @Column(name = "link", nullable = false, unique = true)
    private String link;

    @Column(name = "last_update", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime lastUpdate;

    @Column(name = "deleted")
    private boolean deleted = false;

    @Column(name = "checking")
    private boolean checking = false;

    @OneToMany(mappedBy = "link")
    private List<JpaLinkData> linksData;

    public Link(String link) {
        this.link = link;
        this.lastUpdate = LocalDateTime.now(ZoneOffset.UTC);
    }

    public Link(Long id, String link, LocalDateTime lastUpdate) {
        this.id = id;
        this.link = link;
        this.lastUpdate = lastUpdate;
    }

    public Link(Long id, String link, LocalDateTime lastUpdate, boolean deleted) {
        this.id = id;
        this.link = link;
        this.lastUpdate = lastUpdate;
        this.deleted = deleted;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Link link1)) return false;
        return Objects.equals(link, link1.link) && deleted == link1.deleted;
    }

    @Override
    public int hashCode() {
        return Objects.hash(link, deleted);
    }
}
