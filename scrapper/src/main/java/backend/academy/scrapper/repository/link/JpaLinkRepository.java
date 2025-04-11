package backend.academy.scrapper.repository.link;

import backend.academy.scrapper.entity.Link;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@ConditionalOnProperty(havingValue = "ORM", prefix = "app", name = "access-type")
public interface JpaLinkRepository extends LinkRepository, JpaRepository<Link, Long> {

    @Override
    @Query(value = "select l from Link l where l.deleted = false")
    List<Link> getAll();

    @Override
    @Query(value = "select l from Link l where l.deleted = false and l.id in :ids")
    List<Link> getAllByIds(@Param("ids") List<Long> ids);

    @Override
    @Transactional
    default List<Link> getNotChecked(long limit, LocalDateTime curTime, long checkInterval) {
        List<Link> links = getAllNotCheckedSync(limit, curTime.minusSeconds(checkInterval));
        fetchByIdList(links.stream().map(Link::id).toList());
        return links;
    }

    @Query(value = "select l from Link l join fetch l.linksData where l.id in :ids")
    List<Link> fetchByIdList(@Param("ids") List<Long> ids);

    @Override
    @Query(value = "select l from Link l where l.id = :id and l.deleted = false")
    Optional<Link> getById(@Param("id") long id);

    @Override
    @Query(value = "select l from Link l where l.link = :link and l.deleted = false")
    Optional<Link> getByLink(String link);

    @Override
    @Transactional
    default void create(Link link) {
        Optional<Link> data = getByLinkWithDeletedSync(link.link());
        data.ifPresent(l -> link.id(l.id()));
        save(link);
    }

    @Override
    @Transactional
    default void update(Link link) {
        update(link.link(), link.lastUpdate(), link.id(), link.checking());
    }

    @Override
    @Modifying
    @Transactional
    @Query(value = "update Link l set l.deleted = true where l.id = :id")
    void deleteById(@Param("id") long id);

    @Override
    @Transactional
    default void deleteLink(Link link) {
        delete(link.link());
    }

    @Modifying
    @Transactional
    @Query(
            value =
                    """
            update links
            set checking = true
            where id in (select id from links where deleted = false and last_update < :curTime and checking = false
                limit :limit for update nowait)
            returning *
            """,
            nativeQuery = true)
    List<Link> getAllNotCheckedSync(@Param("limit") long limit, @Param("curTime") LocalDateTime curTime);

    @Query(value = "select l from Link l where l.link = :link")
    Optional<Link> getByLinkWithDeletedSync(@Param("link") String link);

    @Modifying
    @Transactional
    @Query(
            value =
                    "update Link l set l.link = :link, l.lastUpdate = :lastUpdate, l.checking = :checking where l.id = :id and l.deleted = false")
    void update(
            @Param("link") String link,
            @Param("lastUpdate") LocalDateTime lastUpdate,
            @Param("id") long id,
            @Param("checking") boolean checking);

    @Modifying
    @Transactional
    @Query(value = "update Link l set l.deleted = true where l.link = :link")
    void delete(@Param("link") String link);
}
