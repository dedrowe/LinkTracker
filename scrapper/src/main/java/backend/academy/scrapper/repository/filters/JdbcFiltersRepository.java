package backend.academy.scrapper.repository.filters;

import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.entity.jdbc.JdbcFilter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
@ConditionalOnProperty(havingValue = "SQL", prefix = "app", name = "access-type")
public class JdbcFiltersRepository implements FiltersRepository {

    private final JdbcClient jdbcClient;

    @Override
    @Async
    public CompletableFuture<List<JdbcFilter>> getAllByDataId(long dataId) {
        String query = "select * from filters where data_id = :dataId";

        return CompletableFuture.completedFuture(jdbcClient
                .sql(query)
                .param("dataId", dataId)
                .query(JdbcFilter.class)
                .list());
    }

    @Override
    @Async
    public CompletableFuture<Void> create(Filter filter) {
        String query = "insert into filters (data_id, filter) values (:dataId, :filter) returning id";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient
                .sql(query)
                .param("dataId", filter.dataId())
                .param("filter", filter.filter())
                .update(keyHolder);
        filter.id(keyHolder.getKeyAs(Long.class));

        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> deleteById(long id) {
        String query = "delete from filters where id = :id";

        jdbcClient.sql(query).param("id", id).update();

        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> deleteAllByDataId(long dataId) {
        String query = "delete from filters where data_id = :dataId";

        jdbcClient.sql(query).param("dataId", dataId).update();

        return CompletableFuture.completedFuture(null);
    }
}
