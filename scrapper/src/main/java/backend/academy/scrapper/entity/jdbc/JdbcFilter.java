package backend.academy.scrapper.entity.jdbc;

import backend.academy.scrapper.entity.Filter;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof JdbcFilter that)) return false;
        return Objects.equals(dataId, that.dataId) && Objects.equals(filter, that.filter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataId, filter);
    }
}
