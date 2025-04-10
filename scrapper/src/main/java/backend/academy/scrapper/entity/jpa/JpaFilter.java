package backend.academy.scrapper.entity.jpa;

import backend.academy.scrapper.entity.Filter;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "filters")
public class JpaFilter implements Filter {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "filters_id_gen")
    @SequenceGenerator(name = "filters_id_gen", allocationSize = 1, sequenceName = "filters_id_seq")
    protected Long id;

    @NaturalId
    @Column(name = "data_id", nullable = false, insertable = false, updatable = false)
    protected Long dataId;

    @NaturalId
    @Column(name = "filter", nullable = false)
    protected String filter;

    @ManyToOne
    @JoinColumn(name = "data_id", nullable = false, referencedColumnName = "id")
    private JpaLinkData linkData;

    @Override
    public Long id() {
        return id;
    }

    @Override
    public void id(Long id) {
        this.id = id;
    }

    @Override
    public String filter() {
        return filter;
    }

    @Override
    public void filter(String filter) {
        this.filter = filter;
    }

    @Override
    public Long dataId() {
        if (linkData == null) {
            return null;
        }
        return linkData.id();
    }

    public JpaFilter(JpaLinkData data, String filter) {
        this.linkData = data;
        this.filter = filter;
    }

    public JpaFilter(Long id, JpaLinkData data, String filter) {
        this.id = id;
        this.linkData = data;
        this.filter = filter;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        JpaFilter jpaFilter = (JpaFilter) o;
        return Objects.equals(linkData, jpaFilter.linkData) && Objects.equals(filter, jpaFilter.filter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(linkData, filter);
    }
}
