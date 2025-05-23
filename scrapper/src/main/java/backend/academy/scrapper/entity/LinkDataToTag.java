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
import org.hibernate.annotations.NaturalId;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "links_data_to_tags")
public class LinkDataToTag {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "links_data_to_tags_id_gen")
    @SequenceGenerator(
            name = "links_data_to_tags_id_gen",
            allocationSize = 1,
            sequenceName = "links_data_to_tags_id_seq")
    private Long id;

    @NaturalId
    @Column(name = "data_id", nullable = false)
    private long dataId;

    @NaturalId
    @Column(name = "tag_id", nullable = false)
    private long tagId;

    public LinkDataToTag(long dataId, long tagId) {
        this.dataId = dataId;
        this.tagId = tagId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LinkDataToTag that)) return false;
        return dataId == that.dataId && tagId == that.tagId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataId, tagId);
    }
}
