package backend.academy.scrapper.repository.link;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.exceptionHandling.exceptions.LinkException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@ConditionalOnProperty(havingValue = "ORM", prefix = "app", name = "access-type")
public interface JpaLinkRepository extends LinkRepository, Repository<Link, Long> {

    @Override
    @Async
    default CompletableFuture<List<Link>> getAll() {
        return CompletableFuture.completedFuture(getAllSync());
    }

    @Override
    @Async
    default CompletableFuture<List<Link>> getAll(long skip, long limit) {
        return CompletableFuture.completedFuture(getAllSync(skip, limit));
    }

    @Override
    @Async
    default CompletableFuture<List<Link>> getAllNotChecked(long skip, long limit, LocalDateTime curTime, long checkInterval) {
        return CompletableFuture.completedFuture(getAllNotCheckedSync(skip, limit, curTime, checkInterval));
    }

    @Override
    @Async
    default CompletableFuture<Optional<Link>> getById(long id) {
        return CompletableFuture.completedFuture(getByIdSync(id));
    }

    @Override
    @Async
    default CompletableFuture<Optional<Link>> getByLink(String link) {
        return CompletableFuture.completedFuture(getByLinkSync(link));
    }

    @Override
    @Async
    @Transactional
    default CompletableFuture<Void> create(Link link) {
        createSync(link);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    @Transactional
    default CompletableFuture<Void> update(Link link) {
        updateSync(link.link(), link.lastUpdate(), link.id());
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    @Transactional
    default CompletableFuture<Void> deleteById(long id) {
        deleteByIdSync(id);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    @Transactional
    default CompletableFuture<Void> delete(Link link) {
        deleteSync(link.link());
        return CompletableFuture.completedFuture(null);
    }

    @Query(value = "select l from Link l where l.deleted = false")
    List<Link> getAllSync();

    @Query(value = "select * from links where deleted = false offset :skip limit :limit", nativeQuery = true)
    List<Link> getAllSync(@Param("skip") long skip, @Param("limit") long limit);

    @Query(value = "select * from links where deleted = false and extract(epoch from(:curTime - last_update)) > :checkInterval offset :skip limit :limit", nativeQuery = true)
    List<Link> getAllNotCheckedSync(@Param("skip") long skip, @Param("limit") long limit, @Param("curTime") LocalDateTime curTime, @Param("checkInterval") long checkInterval);

    @Query(value = "select l from Link l where l.id = :id and l.deleted = false")
    Optional<Link> getByIdSync(@Param("id") long id);

    @Query(value = "select l from Link l where l.link = :link and l.deleted = false")
    Optional<Link> getByLinkSync(@Param("link") String link);

    @Query(value = "select l from Link l where l.link = :link")
    Optional<Link> getByLinkWithDeletedSync(@Param("link") String link);

    @Transactional
    default void createSync(Link link) {
        Optional<Link> data = getByLinkWithDeletedSync(link.link());

        if (data.isPresent()) {
            if (!data.orElseThrow().deleted()) {
                throw new LinkException("Эта ссылка уже существует", link.link());
            }
            restoreLinkSync(link.link());
        } else {
            insertLinkSync(link.link(), link.lastUpdate());
        }
        Link newLink = getByLinkSync(link.link()).orElseThrow();
        link.id(newLink.id());
    }

    @Modifying
    @Transactional
    @Query(value = "update Link l set l.deleted = false where l.link = :link")
    void restoreLinkSync(@Param("link") String link);

    @Modifying
    @Transactional
    @Query(value = "insert into Link (link, lastUpdate) values (:link, :lastUpdate)")
    void insertLinkSync(@Param("link") String link, @Param("lastUpdate") LocalDateTime lastUpdate);

    @Modifying
    @Transactional
    @Query(value = "update Link l set l.link = :link, l.lastUpdate = :lastUpdate where l.id = :id and l.deleted = false")
    void updateSync(@Param("link") String link, @Param("lastUpdate") LocalDateTime lastUpdate, @Param("id") long id);

    @Modifying
    @Transactional
    @Query(value = "update Link l set l.deleted = true where l.id = :id")
    void deleteByIdSync(@Param("id") long id);

    @Modifying
    @Transactional
    @Query(value = "update Link l set l.deleted = true where l.link = :link")
    void deleteSync(@Param("link") String link);
}
