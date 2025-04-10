package backend.academy.scrapper.repository.tgchat;

import backend.academy.scrapper.entity.TgChat;
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
public class JdbcTgChatRepository implements TgChatRepository {

    private final JdbcClient jdbcClient;

    @Override
    public Optional<TgChat> getById(long id) {
        String query = "select * from tg_chats where id = :id and deleted = false";

        return jdbcClient.sql(query).param("id", id).query(TgChat.class).optional();
    }

    @Override
    public Optional<TgChat> getByChatId(long chatId) {
        String query = "select * from tg_chats where chat_id = :chatId and deleted = false";

        return jdbcClient.sql(query).param("chatId", chatId).query(TgChat.class).optional();
    }

    @Override
    public void create(TgChat tgChat) {
        String query =
                """
            insert into tg_chats (chat_id) values (:chatId)
            on conflict (chat_id) do update set deleted = false
            returning id;
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql(query).param("chatId", tgChat.chatId()).update(keyHolder);
        tgChat.id(keyHolder.getKeyAs(Long.class));
    }

    @Override
    public void deleteById(long id) {
        String query = "update tg_chats set deleted = true where id = :id";

        jdbcClient.sql(query).param("id", id).update();
    }

    @Override
    public void delete(TgChat tgChat) {
        String query = "update tg_chats set deleted = true where chat_id = :chatId";

        jdbcClient.sql(query).param("chatId", tgChat.chatId()).update();
    }
}
