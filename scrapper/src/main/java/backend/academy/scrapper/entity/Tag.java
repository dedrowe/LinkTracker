package backend.academy.scrapper.entity;

import backend.academy.scrapper.entity.jpa.JpaLinkData;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.List;
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
@Table(name = "tags")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tags_id_gen")
    @SequenceGenerator(name = "tags_id_gen", allocationSize = 1, sequenceName = "tags_id_seq")
    private Long id;

    @Column(name = "tag", nullable = false, unique = true)
    private String tag;

    @ManyToMany(mappedBy = "tags")
    private List<JpaLinkData> linksData;

    public Tag(String tag) {
        this.tag = tag;
    }

    public Tag(Long id, String tag) {
        this.id = id;
        this.tag = tag;
    }
}
