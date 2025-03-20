package backend.academy.scrapper.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "filters")
public class Filter {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "filters_id_gen")
    @SequenceGenerator(name = "filters_id_gen", allocationSize = 1, sequenceName = "filters_id_seq")
    private Long id;

    @Column(name = "data_id", nullable = false, insertable = false, updatable = false)
    private Long dataId;

    @Column(name = "filter", nullable = false)
    private String filter;

    @ManyToOne
    @JoinColumn(name = "data_id", nullable = false, referencedColumnName = "id")
    private LinkData linkData;

    public Filter(Long dataId, String filter) {
        this.dataId = dataId;
        this.filter = filter;
    }

    public Filter(Long id, long dataId, String filter) {
        this.id = id;
        this.dataId = dataId;
        this.filter = filter;
    }

    public Filter(LinkData data, String filter) {
        this.linkData = data;
        this.filter = filter;
    }

    public Filter(Long id, LinkData data, String filter) {
        this.id = id;
        this.linkData = data;
        this.filter = filter;
    }
}
