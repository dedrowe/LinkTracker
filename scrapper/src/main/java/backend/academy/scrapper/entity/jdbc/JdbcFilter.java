package backend.academy.scrapper.entity.jdbc;

import backend.academy.scrapper.entity.Filter;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class JdbcFilter implements Filter {

    protected Long id;

    protected Long dataId;

    protected String filter;

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
        return dataId;
    }

    public JdbcFilter(Long dataId, String filter) {
        this.dataId = dataId;
        this.filter = filter;
    }
}
