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
@Table(name = "filters")
public class Filter {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
        generator = "filters_id_gen")
    @SequenceGenerator(name = "filters_id_gen",
        allocationSize = 1,
        sequenceName = "filters_id_seq")
    private Long id;

    @Column(name = "data_id", nullable = false)
    private long dataId;

    @Column(name = "filter", nullable = false)
    private String filter;

    public Filter(long dataId, String filter) {
        this.dataId = dataId;
        this.filter = filter;
    }
}
