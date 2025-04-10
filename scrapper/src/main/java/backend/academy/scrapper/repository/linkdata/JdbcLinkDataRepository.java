package backend.academy.scrapper.repository.linkdata;

import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.entity.jdbc.JdbcLinkData;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@ConditionalOnProperty(havingValue = "SQL", prefix = "app", name = "access-type")
@AllArgsConstructor
public class JdbcLinkDataRepository implements LinkDataRepository {

    private final JdbcClient jdbcClient;

    @Override
    public List<JdbcLinkData> getAll() {
        String query = "select * from links_data where deleted = false";

        return jdbcClient.sql(query).query(JdbcLinkData.class).list();
    }

    @Override
    public Optional<JdbcLinkData> getById(long id) {
        String query = "select * from links_data where id = :id and deleted = false";

        return jdbcClient.sql(query).param("id", id).query(JdbcLinkData.class).optional();
    }

    @Override
    public List<JdbcLinkData> getByChatId(long chatId) {
        String query = "select * from links_data where chat_id = :chatId and deleted = false";

        return jdbcClient
                .sql(query)
                .param("chatId", chatId)
                .query(JdbcLinkData.class)
                .list();
    }

    @Override
    public List<JdbcLinkData> getByLinkId(long linkId) {
        String query = "select * from links_data where link_id = :linkId and deleted = false";

        return jdbcClient
                .sql(query)
                .param("linkId", linkId)
                .query(JdbcLinkData.class)
                .list();
    }

    @Override
    public List<JdbcLinkData> getByLinkId(long linkId, long minId, long limit) {
        String query =
                "select * from links_data where link_id = :linkId and deleted = false and id > :minId limit :limit";

        return jdbcClient
                .sql(query)
                .param("linkId", linkId)
                .param("minId", minId)
                .param("limit", limit)
                .query(JdbcLinkData.class)
                .list();
    }

    @Override
    public Optional<JdbcLinkData> getByChatIdLinkId(long chatId, long linkId) {
        String query = "select * from links_data where chat_id = :chatId AND link_id = :linkId and deleted = false";

        return jdbcClient
                .sql(query)
                .param("chatId", chatId)
                .param("linkId", linkId)
                .query(JdbcLinkData.class)
                .optional();
    }

    @Override
    public List<JdbcLinkData> getByTagAndChatId(String tag, long chatId) {
        String query =
                "select links_data.id, links_data.chat_id, links_data.link_id, links_data.deleted " + "from links_data "
                        + "join tg_chats on links_data.chat_id = tg_chats.id "
                        + "join links_data_to_tags on links_data.id = links_data_to_tags.data_id "
                        + "join tags on links_data_to_tags.tag_id = tags.id "
                        + "where tags.tag = :tag and tg_chats.chat_id = :chatId and links_data.deleted = false";

        return jdbcClient
                .sql(query)
                .param("tag", tag)
                .param("chatId", chatId)
                .query(JdbcLinkData.class)
                .list();
    }

    @Override
    public void create(LinkData linkData) {
        String query =
                """
            insert into links_data (link_id, chat_id) values (:linkId, :chatId)
            on conflict (link_id, chat_id) do update set deleted = false
            returning id;
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient
                .sql(query)
                .param("linkId", linkData.linkId())
                .param("chatId", linkData.chatId())
                .update(keyHolder);
        linkData.id(keyHolder.getKeyAs(Long.class));
    }

    @Override
    public void deleteById(long id) {
        String query = "update links_data set deleted = true where id = :id";

        jdbcClient.sql(query).param("id", id).update();
    }

    @Override
    public void deleteLinkData(LinkData link) {
        String query = "update links_data set deleted = true where link_id = :linkId and chat_id = :chatId";

        jdbcClient
                .sql(query)
                .param("linkId", link.linkId())
                .param("chatId", link.chatId())
                .update();
    }

    @Override
    public void deleteByChatId(long chatId) {
        String query = "update links_data set deleted = true where chat_id = :chatId";

        jdbcClient.sql(query).param("chatId", chatId).update();
    }
}
