package backend.academy.scrapper.repository.filters;

import backend.academy.scrapper.entity.Filter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

@ConditionalOnProperty(havingValue = "ORM", prefix = "app", name = "access-type")
public interface JpaFiltersRepository extends FiltersRepository, Repository<Filter, Long> {

    @Override
    @Async
    default CompletableFuture<List<Filter>> getAllByDataId(long dataId) {
        return CompletableFuture.completedFuture(getAllByDataIdSync(dataId));
    }

    @Override
    @Async
    @Transactional
    default CompletableFuture<Void> createAll(List<String> filters, long linkDataId) {
        createAllSync(filters, linkDataId);
        return null;
    }

    @Override
    @Async
    @Transactional
    default CompletableFuture<Void> deleteAllByDataId(long dataId) {
        deleteAllByDataIdSync(dataId);
        return null;
    }

    @Query(value = "select f from Filter f where f.dataId = :dataId")
    List<Filter> getAllByDataIdSync(@Param("dataId") long dataId);

    @Transactional
    default void createAllSync(List<String> filters, long linkDataId) {
        List<Filter> curFilters = getAllByDataIdSync(linkDataId);
        Set<String> filtersSet = new HashSet<>(filters);
        Set<Filter> curFiltersSet = new HashSet<>(curFilters);

        for (Filter filter : Set.copyOf(curFiltersSet)) {
            if (filtersSet.contains(filter.filter())) {
                curFiltersSet.remove(filter);
                filtersSet.remove(filter.filter());
            }
        }
        for (Filter filter : curFiltersSet) {
            deleteByIdSync(filter.id());
        }
        for (String filter : filtersSet) {
            insertFilterSync(linkDataId, filter);
        }
    }

    @Modifying
    @Transactional
    @Query(value = "insert into Filter (dataId, filter) values (:dataId, :filter)")
    void insertFilterSync(@Param("dataId") long dataId, @Param("filter") String filter);

    @Modifying
    @Transactional
    @Query(value = "delete from Filter f where f.id = :id")
    void deleteByIdSync(@Param("id") long id);

    @Modifying
    @Transactional
    @Query(value = "delete from Filter f where f.dataId = :dataId")
    void deleteAllByDataIdSync(@Param("dataId") long dataId);
}
