package backend.academy.scrapper.repository.filters;

import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.entity.jpa.JpaFilter;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(havingValue = "ORM", prefix = "app", name = "access-type")
public interface JpaFiltersRepository extends FiltersRepository, JpaRepository<JpaFilter, Long> {

    @Override
    @Query(value = "select f from JpaFilter f where f.dataId = :dataId")
    List<JpaFilter> getAllByDataId(@Param("dataId") long dataId);

    @Override
    @Transactional
    default void create(Filter filter) {
        save((JpaFilter) filter);
        flush();
    }

    @Override
    @Modifying
    @Transactional
    @Query(value = "delete from JpaFilter f where f.id = :id")
    void deleteById(@Param("id") long id);

    @Override
    @Modifying
    @Transactional
    @Query(value = "delete from JpaFilter f where f.dataId = :dataId")
    void deleteAllByDataId(@Param("dataId") long dataId);
}
