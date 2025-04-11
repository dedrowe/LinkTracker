package backend.academy.scrapper.repository.link;

import backend.academy.scrapper.entity.Link;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Slf4j
@ConditionalOnProperty(havingValue = "SQL", prefix = "app", name = "access-type")
@AllArgsConstructor
@Transactional
public class JdbcLinkRepository implements LinkRepository {

    private final JdbcClient jdbcClient;

    @Override
    public List<Link> getAll() {
        String query = "select * from links where deleted = false";

        return jdbcClient.sql(query).query(Link.class).list();
    }

    @Override
    public List<Link> getAllByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }

        String query = "select * from links where deleted = false and id in (:ids)";

        return jdbcClient.sql(query).param("ids", ids).query(Link.class).list();
    }

    @Override
    public List<Link> getNotChecked(long limit, LocalDateTime curTime, long checkInterval) {
        String query =
                """
        update links
        set checking = true
        where id in (select id from links where deleted = false and last_update < :curTime and checking = false
            limit :limit for update nowait)
        returning *
        """;

        return jdbcClient
                .sql(query)
                .param("limit", limit)
                .param("curTime", curTime.minusSeconds(checkInterval))
                .query(Link.class)
                .list();
    }

    @Override
    public Optional<Link> getById(long id) {
        String query = "select * from links where id = :id and deleted = false";

        return jdbcClient.sql(query).param("id", id).query(Link.class).optional();
    }

    @Override
    public Optional<Link> getByLink(String link) {
        String query = "select * from links where link = :link and deleted = false";

        return jdbcClient.sql(query).param("link", link).query(Link.class).optional();
    }

    @Override
    public void create(Link link) {
        String query =
                """
            insert into links (link, last_update) VALUES (:link, :last_update)
            on conflict (link) do update set deleted = false
            returning id;
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient
                .sql(query)
                .param("link", link.link())
                .param("last_update", link.lastUpdate())
                .update(keyHolder);
        link.id(keyHolder.getKeyAs(Long.class));
    }

    @Override
    public void update(Link link) {
        String query =
                "update links set link = :link, last_update = :last_update, checking = :checking where id = :id and deleted = false";

        jdbcClient
                .sql(query)
                .param("link", link.link())
                .param("last_update", link.lastUpdate())
                .param("id", link.id())
                .param("checking", link.checking())
                .update();
    }

    @Override
    public void deleteById(long id) {
        String query = "update links set deleted = true where id = :id";

        jdbcClient.sql(query).param("id", id).update();
    }

    @Override
    public void deleteLink(Link link) {
        String query = "update links set deleted = true where link = :link";

        jdbcClient.sql(query).param("link", link.link()).update();
    }
}
