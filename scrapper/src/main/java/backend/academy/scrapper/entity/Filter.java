package backend.academy.scrapper.entity;

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
public class Filter {

    private Long id;

    private long data_id;

    private String filter;

    private boolean deleted = false;

    public Filter(long data_id, String filter) {
        this.data_id = data_id;
        this.filter = filter;
    }

    public Filter(long data_id, String filter, boolean deleted) {
        this.data_id = data_id;
        this.filter = filter;
        this.deleted = deleted;
    }
}
