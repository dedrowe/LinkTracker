package backend.academy.scrapper.repository.filters;

import backend.academy.scrapper.entity.Filter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

@ConditionalOnProperty(havingValue = "ORM", prefix = "app", name = "access-type")
public interface JpaFiltersRepository extends FiltersRepository, CrudRepository<Filter, Long> {

    @Override
    @Async
    default CompletableFuture<List<Filter>> getAllByDataId(long dataId) {
        return CompletableFuture.completedFuture(getAllByDataIdSync(dataId));
    }

    @Query(value = "select f from Filter f where f.dataId = :dataId")
    List<Filter> getAllByDataIdSync(@Param("dataId") long dataId);

    @Override
    @Async
    @Transactional
    default CompletableFuture<Void> create(Filter filter) {
        createSync(filter);
        return CompletableFuture.completedFuture(null);
    }

    @Transactional
    default void createSync(Filter filter) {
        save(filter);
    }

    @Override
    @Async
    @Transactional
    default CompletableFuture<Void> deleteById(long id) {
        deleteByIdSync(id);
        return CompletableFuture.completedFuture(null);
    }

    @Modifying
    @Transactional
    @Query(value = "delete from Filter f where f.id = :id")
    void deleteByIdSync(@Param("id") long id);

    @Override
    @Async
    @Transactional
    default CompletableFuture<Void> deleteAllByDataId(long dataId) {
        deleteAllByDataIdSync(dataId);
        return null;
    }

    @Modifying
    @Transactional
    @Query(value = "delete from Filter f where f.dataId = :dataId")
    void deleteAllByDataIdSync(@Param("dataId") long dataId);
}
