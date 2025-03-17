package backend.academy.scrapper.repository.filters;

import backend.academy.scrapper.entity.Filter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
@ConditionalOnProperty(havingValue = "SQL", prefix = "app", name = "access-type")
public class JdbcFiltersRepository implements FiltersRepository {

    private final JdbcClient jdbcClient;

    @Override
    @Async
    public CompletableFuture<List<Filter>> getAllByDataId(long dataId) {
        return CompletableFuture.completedFuture(getByDataIdInternal(dataId));
    }

    @Override
    @Async
    public CompletableFuture<Void> createAll(List<String> filters, long linkDataId) {
        List<Filter> curFilters = getByDataIdInternal(linkDataId);
        Set<String> filtersSet = new HashSet<>(filters);
        Set<Filter> curFiltersSet = new HashSet<>(curFilters);

        for (Filter filter : Set.copyOf(curFiltersSet)) {
            if (filtersSet.contains(filter.filter())) {
                curFiltersSet.remove(filter);
                filtersSet.remove(filter.filter());
            }
        }
        for (Filter filter : curFiltersSet) {
            deleteById(filter.id());
        }
        for (String filter : filtersSet) {
            createFilter(linkDataId, filter);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> deleteAllByDataId(long dataId) {
        jdbcClient
                .sql("DELETE FROM filters WHERE data_id = :dataId")
                .param("dataId", dataId)
                .update();
        return CompletableFuture.completedFuture(null);
    }

    private List<Filter> getByDataIdInternal(long dataId) {
        String query = "SELECT * FROM filters WHERE data_id = :dataId";

        return jdbcClient.sql(query).param("dataId", dataId).query(Filter.class).list();
    }

    private void createFilter(long dataId, String filter) {
        String query = "INSERT INTO filters (data_id, filter) VALUES (:dataId, :filter)";

        jdbcClient.sql(query).param("dataId", dataId).param("filter", filter).update();
    }

    private void deleteById(long id) {
        String query = "DELETE FROM filters WHERE id = :id";

        jdbcClient.sql(query).param("id", id).update();
    }
}
