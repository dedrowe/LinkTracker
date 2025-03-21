package backend.academy.scrapper.repository.linkdata;

import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.exceptionHandling.exceptions.LinkDataException;
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

    @Override
    @Async
    public CompletableFuture<List<LinkData>> getAll() {
        String query = "select * from links_data where deleted = false";

        List<LinkData> linksData = jdbcClient.sql(query).query(LinkData.class).list();

        return CompletableFuture.completedFuture(linksData);
    }

    @Override
    @Async
    public CompletableFuture<Optional<LinkData>> getById(long id) {
        String query = "select * from links_data where id = :id and deleted = false";

        Optional<LinkData> linkData =
                jdbcClient.sql(query).param("id", id).query(LinkData.class).optional();

        return CompletableFuture.completedFuture(linkData);
    }

    @Override
    @Async
    public CompletableFuture<List<LinkData>> getByChatId(long chatId) {
        String query = "select * from links_data where chat_id = :chatId and deleted = false";

        List<LinkData> linksData = jdbcClient
                .sql(query)
                .param("chatId", chatId)
                .query(LinkData.class)
                .list();

        return CompletableFuture.completedFuture(linksData);
    }

    @Override
    @Async
    public CompletableFuture<List<LinkData>> getByLinkId(long linkId) {
        String query = "select * from links_data where link_id = :linkId and deleted = false";

        List<LinkData> linksData = jdbcClient
                .sql(query)
                .param("linkId", linkId)
                .query(LinkData.class)
                .list();

        return CompletableFuture.completedFuture(linksData);
    }

    @Override
    public CompletableFuture<List<LinkData>> getByLinkId(long linkId, long skip, long limit) {
        String query =
                "select * from links_data where link_id = :linkId and deleted = false offset :skip limit :limit for update";

        List<LinkData> linksData = jdbcClient
                .sql(query)
                .param("linkId", linkId)
                .param("skip", skip)
                .param("limit", limit)
                .query(LinkData.class)
                .list();

        return CompletableFuture.completedFuture(linksData);
    }

    @Override
    @Async
    public CompletableFuture<Optional<LinkData>> getByChatIdLinkId(long chatId, long linkId) {
        String query = "select * from links_data where chat_id = :chatId AND link_id = :linkId and deleted = false";

        Optional<LinkData> linkData = jdbcClient
                .sql(query)
                .param("chatId", chatId)
                .param("linkId", linkId)
                .query(LinkData.class)
                .optional();

        return CompletableFuture.completedFuture(linkData);
    }

    @Override
    @Async
    public CompletableFuture<List<LinkData>> getByTagAndChatId(String tag, long chatId) {
        String query =
                "select links_data.id, links_data.chat_id, links_data.link_id, links_data.deleted " + "from links_data "
                        + "join tg_chats on links_data.chat_id = tg_chats.id "
                        + "join links_data_to_tags on links_data.id = links_data_to_tags.data_id "
                        + "join tags on links_data_to_tags.tag_id = tags.id "
                        + "where tags.tag = :tag and tg_chats.chat_id = :chatId and links_data.deleted = false";

        return CompletableFuture.completedFuture(jdbcClient
                .sql(query)
                .param("tag", tag)
                .param("chatId", chatId)
                .query(LinkData.class)
                .list());
    }

    @Override
    @Async
    public CompletableFuture<Void> create(LinkData linkData) {
        Optional<LinkData> data = getByChatIdLinkIdWithDeleted(linkData.chatId(), linkData.linkId());

        if (data.isPresent()) {
            if (!data.orElseThrow().deleted()) {
                throw new LinkDataException(
                        "Ссылка уже зарегистрирована",
                        String.valueOf(linkData.linkId()),
                        String.valueOf(linkData.chatId()));
            }

            restoreLinkData(data.orElseThrow().id());
            linkData.id(data.orElseThrow().id());
        } else {
            linkData.id(createInternal(linkData));
        }

        return CompletableFuture.completedFuture(null);
    }

    private Long createInternal(LinkData linkData) {
        String query = "insert into links_data (chat_id, link_id) values (:chatId, :linkId) returning id";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient
            .sql(query)
            .param("chatId", linkData.chatId())
            .param("linkId", linkData.linkId())
            .update(keyHolder);
        return keyHolder.getKeyAs(Long.class);
    }

    private Optional<LinkData> getByChatIdLinkIdWithDeleted(long chatId, long linkId) {
        String getQuery = "select * from links_data where chat_id = :chatId and link_id = :linkId";

        return jdbcClient
            .sql(getQuery)
            .param("chatId", chatId)
            .param("linkId", linkId)
            .query(LinkData.class)
            .optional();
    }

    private void restoreLinkData(long id) {
        String restoreQuery = "update links_data set deleted = false where id = :id";

        jdbcClient.sql(restoreQuery).param("id", id).update();
    }

    @Override
    @Async
    public CompletableFuture<Void> deleteById(long id) {
        String query = "update links_data set deleted = true where id = :id";

        jdbcClient.sql(query).param("id", id).update();

        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> deleteLinkData(LinkData link) {
        String query = "update links_data set deleted = true where link_id = :linkId and chat_id = :chatId";

        jdbcClient
                .sql(query)
                .param("linkId", link.linkId())
                .param("chatId", link.chatId())
                .update();

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> deleteByChatId(long chatId) {
        String query = "update links_data set deleted = true where chat_id = :chatId";

        jdbcClient.sql(query).param("chatId", chatId).update();

        return CompletableFuture.completedFuture(null);
    }
}
