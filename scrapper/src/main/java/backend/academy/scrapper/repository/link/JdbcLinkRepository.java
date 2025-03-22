package backend.academy.scrapper.repository.link;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.exceptionHandling.exceptions.LinkException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@ConditionalOnProperty(havingValue = "SQL", prefix = "app", name = "access-type")
public class JdbcLinkRepository implements LinkRepository {

    private final JdbcClient jdbcClient;

    @Autowired
    public JdbcLinkRepository(JdbcClient client) {
        jdbcClient = client;
    }

    @Override
    @Async
    public CompletableFuture<List<Link>> getAll() {
        String query = "select * from links where deleted = false";

        List<Link> links = jdbcClient.sql(query).query(Link.class).list();

        return CompletableFuture.completedFuture(links);
    }

    @Override
    public CompletableFuture<List<Link>> getAll(long skip, long limit) {
        String query = "select * from links where deleted = false offset :skip limit :limit";

        List<Link> links = jdbcClient
                .sql(query)
                .param("skip", skip)
                .param("limit", limit)
                .query(Link.class)
                .list();

        return CompletableFuture.completedFuture(links);
    }

    @Override
    public CompletableFuture<List<Link>> getAllNotChecked(
            long skip, long limit, LocalDateTime curTime, long checkInterval) {
        String query = "select * from links where deleted = false and :curTime > last_update offset :skip limit :limit";

        List<Link> links = jdbcClient
                .sql(query)
                .param("skip", skip)
                .param("limit", limit)
                .param("curTime", curTime.minusSeconds(checkInterval))
                .query(Link.class)
                .list();

        return CompletableFuture.completedFuture(links);
    }

    @Override
    @Async
    public CompletableFuture<Optional<Link>> getById(long id) {
        String query = "select * from links where id = :id and deleted = false";

        Optional<Link> link =
                jdbcClient.sql(query).param("id", id).query(Link.class).optional();

        return CompletableFuture.completedFuture(link);
    }

    @Override
    @Async
    public CompletableFuture<Optional<Link>> getByLink(String link) {
        String query = "select * from links where link = :link and deleted = false";

        Optional<Link> result =
                jdbcClient.sql(query).param("link", link).query(Link.class).optional();

        return CompletableFuture.completedFuture(result);
    }

    @Override
    @Async
    public CompletableFuture<Void> create(Link link) {
        Optional<Link> data = getByLinkWithDeleted(link.link());

        if (data.isPresent()) {
            if (!data.orElseThrow().deleted()) {
                throw new LinkException("Эта ссылка уже существует", link.link());
            }

            restoreLink(link.link());
            link.id(data.orElseThrow().id());
        } else {
            link.id(createInternal(link));
        }

        return CompletableFuture.completedFuture(null);
    }

    private Optional<Link> getByLinkWithDeleted(String link) {
        String getQuery = "select * from links where link = :link";

        return jdbcClient.sql(getQuery).param("link", link).query(Link.class).optional();
    }

    private void restoreLink(String link) {
        String restoreQuery = "update links set deleted = false where link = :link";

        jdbcClient.sql(restoreQuery).param("link", link).update();
    }

    private Long createInternal(Link link) {
        String query = "insert into links (link, last_update) values (:link, :last_update) returning id";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient
                .sql(query)
                .param("link", link.link())
                .param("last_update", link.lastUpdate())
                .update(keyHolder);
        return keyHolder.getKeyAs(Long.class);
    }

    @Override
    @Async
    public CompletableFuture<Void> update(Link link) {
        String query = "update links set link = :link, last_update = :last_update where id = :id and deleted = false";

        jdbcClient
                .sql(query)
                .param("link", link.link())
                .param("last_update", link.lastUpdate())
                .param("id", link.id())
                .update();

        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> deleteById(long id) {
        String query = "update links set deleted = true where id = :id";

        jdbcClient.sql(query).param("id", id).update();

        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> deleteLink(Link link) {
        String query = "update links set deleted = true where link = :link";

        jdbcClient.sql(query).param("link", link.link()).update();

        return CompletableFuture.completedFuture(null);
    }
}
