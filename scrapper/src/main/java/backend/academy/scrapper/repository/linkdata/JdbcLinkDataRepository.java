package backend.academy.scrapper.repository.linkdata;

import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.exceptionHandling.exceptions.LinkDataException;
import backend.academy.scrapper.repository.rowMappers.LinkDataRowMapper;
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
public class JdbcLinkDataRepository implements LinkDataRepository {

    private final JdbcClient jdbcClient;

    private final LinkDataRowMapper linkDataRowMapper = new LinkDataRowMapper();

    @Override
    @Async
    public CompletableFuture<List<LinkData>> getAll() {
        String query = "SELECT * FROM links_data WHERE deleted = false";

        List<LinkData> linksData =
                jdbcClient.sql(query).query(linkDataRowMapper).list();

        return CompletableFuture.completedFuture(linksData);
    }

    @Override
    @Async
    public CompletableFuture<Optional<LinkData>> getById(long id) {
        String query = "SELECT * FROM links_data WHERE id = :id and deleted = false";

        Optional<LinkData> linkData =
                jdbcClient.sql(query).param("id", id).query(linkDataRowMapper).optional();

        return CompletableFuture.completedFuture(linkData);
    }

    @Override
    @Async
    public CompletableFuture<List<LinkData>> getByChatId(long chatId) {
        String query = "SELECT * FROM links_data WHERE chat_id = :chatId and deleted = false";

        List<LinkData> linksData = jdbcClient
                .sql(query)
                .param("chatId", chatId)
                .query(linkDataRowMapper)
                .list();

        return CompletableFuture.completedFuture(linksData);
    }

    @Override
    @Async
    public CompletableFuture<List<LinkData>> getByLinkId(long linkId) {
        String query = "SELECT * FROM links_data WHERE link_id = :linkId and deleted = false";

        List<LinkData> linksData = jdbcClient
                .sql(query)
                .param("linkId", linkId)
                .query(linkDataRowMapper)
                .list();

        return CompletableFuture.completedFuture(linksData);
    }

    @Override
    public CompletableFuture<List<LinkData>> getByLinkId(long linkId, long skip, long limit) {
        String query = "SELECT * FROM links_data WHERE link_id = :linkId and deleted = false OFFSET :skip LIMIT :limit";

        List<LinkData> linksData = jdbcClient
                .sql(query)
                .param("linkId", linkId)
                .param("skip", skip)
                .param("limit", limit)
                .query(linkDataRowMapper)
                .list();

        return CompletableFuture.completedFuture(linksData);
    }

    @Override
    @Async
    public CompletableFuture<Optional<LinkData>> getByChatIdLinkId(long chatId, long linkId) {
        String query = "SELECT * FROM links_data WHERE chat_id = :chatId AND link_id = :linkId and deleted = false";

        Optional<LinkData> linkData = jdbcClient
                .sql(query)
                .param("chatId", chatId)
                .param("linkId", linkId)
                .query(linkDataRowMapper)
                .optional();

        return CompletableFuture.completedFuture(linkData);
    }

    @Override
    @Async
    public CompletableFuture<Void> create(LinkData linkData) {
        String getQuery = "SELECT * FROM links_data WHERE chat_id = :chatId and link_id = :linkId";

        Optional<LinkData> data = jdbcClient
                .sql(getQuery)
                .param("chatId", linkData.chatId())
                .param("linkId", linkData.linkId())
                .query(linkDataRowMapper)
                .optional();

        if (data.isPresent()) {
            if (!data.orElseThrow().deleted()) {
                throw new LinkDataException(
                        "Ссылка уже зарегистрирована",
                        String.valueOf(linkData.linkId()),
                        String.valueOf(linkData.chatId()));
            }

            String restoreQuery =
                    "UPDATE links_data SET deleted = false, tags = :tags, filters = :filters WHERE id = :id";

            jdbcClient
                    .sql(restoreQuery)
                    .param("id", data.orElseThrow().id())
                    .param("tags", linkData.tags().toArray(new String[0]))
                    .param("filters", linkData.filters().toArray(new String[0]))
                    .update();
        } else {
            String query =
                    "INSERT INTO links_data (chat_id, link_id, tags, filters) VALUES (:chatId, :linkId, :tags, :filters) RETURNING id";

            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcClient
                    .sql(query)
                    .param("chatId", linkData.chatId())
                    .param("linkId", linkData.linkId())
                    .param("tags", linkData.tags().toArray(new String[0]))
                    .param("filters", linkData.filters().toArray(new String[0]))
                    .update(keyHolder);
            linkData.id(keyHolder.getKeyAs(Long.class));
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> update(LinkData link) {
        String query =
                "UPDATE links_data SET tags = :tags, filters = :filters WHERE chat_id = :chatId and link_id = :linkId and deleted = false";

        jdbcClient
                .sql(query)
                .param("tags", link.tags().toArray(new String[0]))
                .param("filters", link.filters().toArray(new String[0]))
                .param("chatId", link.chatId())
                .param("linkId", link.linkId())
                .update();

        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> deleteById(long id) {
        String query = "UPDATE links_data SET deleted = true WHERE id = :id";

        jdbcClient.sql(query).param("id", id).update();

        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> delete(LinkData link) {
        String query = "UPDATE links_data SET deleted = true WHERE link_id = :linkId and chat_id = :chatId";

        jdbcClient
                .sql(query)
                .param("linkId", link.linkId())
                .param("chatId", link.chatId())
                .update();

        return CompletableFuture.completedFuture(null);
    }
}
