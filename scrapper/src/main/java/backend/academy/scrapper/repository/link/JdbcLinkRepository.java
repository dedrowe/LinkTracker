package backend.academy.scrapper.repository.link;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.exceptionHandling.exceptions.LinkException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@ConditionalOnProperty(havingValue = "SQL", prefix = "app", name = "access-type")
@AllArgsConstructor
public class JdbcLinkRepository implements LinkRepository {

    private final JdbcClient jdbcClient;

    @Override
    @Async
    public CompletableFuture<List<Link>> getAll() {
        String query = "SELECT * FROM links WHERE deleted = false";

        List<Link> links = jdbcClient.sql(query).query(Link.class).list();

        return CompletableFuture.completedFuture(links);
    }

    @Override
    @Async
    public CompletableFuture<Optional<Link>> getById(long id) {
        String query = "SELECT * FROM links WHERE id = :id and deleted = false";

        Optional<Link> link =
                jdbcClient.sql(query).param("id", id).query(Link.class).optional();

        return CompletableFuture.completedFuture(link);
    }

    @Override
    @Async
    public CompletableFuture<Optional<Link>> getByLink(String link) {
        String query = "SELECT * FROM links WHERE link = :link and deleted = false";

        Optional<Link> result =
                jdbcClient.sql(query).param("link", link).query(Link.class).optional();

        return CompletableFuture.completedFuture(result);
    }

    @Override
    @Async
    public CompletableFuture<Void> create(Link link) {
        String getQuery = "SELECT * FROM links WHERE link = :link";

        Optional<Link> data = jdbcClient
                .sql(getQuery)
                .param("link", link.link())
                .query(Link.class)
                .optional();

        if (data.isPresent()) {
            if (!data.orElseThrow().deleted()) {
                throw new LinkException("Эта ссылка уже существует", link.link());
            }

            String restoreQuery = "UPDATE links SET deleted = false WHERE link = :link";

            jdbcClient.sql(restoreQuery).param("link", link.link()).update();
        } else {
            String query = "INSERT INTO links (link, last_update) VALUES (:link, :last_update) RETURNING id";

            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcClient
                    .sql(query)
                    .param("link", link.link())
                    .param("last_update", link.lastUpdate())
                    .update(keyHolder);
            link.id(keyHolder.getKeyAs(Long.class));
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> update(Link link) {
        String query = "UPDATE links SET link = :link, last_update = :last_update WHERE id = :id and deleted = false";

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
        String query = "UPDATE links SET deleted = true WHERE id = :id";

        jdbcClient.sql(query).param("id", id).update();

        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> delete(Link link) {
        String query = "UPDATE links SET deleted = true WHERE link = :link";

        jdbcClient.sql(query).param("link", link.link()).update();

        return CompletableFuture.completedFuture(null);
    }
}
