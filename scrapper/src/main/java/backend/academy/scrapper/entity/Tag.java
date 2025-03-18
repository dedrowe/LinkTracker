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
@Table(name = "tags")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
        generator = "tags_id_gen")
    @SequenceGenerator(name = "tags_id_gen",
        allocationSize = 1,
        sequenceName = "tags_id_seq")
    private Long id;

    @Column(name = "tag", nullable = false, unique = true)
    private String tag;

    public Tag(String tag) {
        this.tag = tag;
    }
}
