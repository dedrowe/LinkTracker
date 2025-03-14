package backend.academy.scrapper.repository.tgchat;

import backend.academy.scrapper.entity.TgChat;
import backend.academy.scrapper.exceptionHandling.exceptions.TgChatException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
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
    public CompletableFuture<Optional<TgChat>> getById(long id) {
        String query = "SELECT * FROM tg_chats WHERE id = :id and deleted = false";

        Optional<TgChat> tgChat =
                jdbcClient.sql(query).param("id", id).query(TgChat.class).optional();

        return CompletableFuture.completedFuture(tgChat);
    }

    @Override
    public CompletableFuture<Optional<TgChat>> getByChatId(long chatId) {
        String query = "SELECT * FROM tg_chats WHERE chat_id = :chatId and deleted = false";

        Optional<TgChat> tgChat = jdbcClient
                .sql(query)
                .param("chatId", chatId)
                .query(TgChat.class)
                .optional();

        return CompletableFuture.completedFuture(tgChat);
    }

    @Override
    public CompletableFuture<Void> create(TgChat tgChat) {
        String getQuery = "SELECT * FROM tg_chats WHERE chat_id = :chatId";

        Optional<TgChat> data = jdbcClient
                .sql(getQuery)
                .param("chatId", tgChat.chatId())
                .query(TgChat.class)
                .optional();

        if (data.isPresent()) {
            if (!data.orElseThrow().deleted()) {
                throw new TgChatException("Чат с таким id уже зарегистрирован", String.valueOf(tgChat.chatId()));
            }

            String restoreQuery = "UPDATE tg_chats SET deleted = false WHERE chat_id = :chatId";

            jdbcClient.sql(restoreQuery).param("chatId", tgChat.chatId()).update();
        } else {
            String query = "INSERT INTO tg_chats (chat_id) VALUES (:chatId) RETURNING id";

            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcClient.sql(query).param("chatId", tgChat.chatId()).update(keyHolder);
            tgChat.id(keyHolder.getKeyAs(Long.class));
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> deleteById(long id) {
        String query = "UPDATE tg_chats SET deleted = true WHERE id = :id";

        jdbcClient.sql(query).param("id", id).update();

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> delete(TgChat tgChat) {
        String query = "UPDATE tg_chats SET deleted = true WHERE chat_id = :chatId";

        jdbcClient.sql(query).param("chatId", tgChat.chatId()).update();

        return CompletableFuture.completedFuture(null);
    }
}
