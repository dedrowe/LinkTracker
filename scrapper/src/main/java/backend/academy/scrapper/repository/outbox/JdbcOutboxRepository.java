package backend.academy.scrapper.repository.outbox;

import backend.academy.scrapper.entity.Outbox;
import backend.academy.scrapper.utils.UtcDateTimeProvider;
import java.util.List;
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
public class JdbcOutboxRepository implements OutboxRepository {

    private final JdbcClient jdbcClient;

    @Override
    public List<Outbox> getAllWithDeletion(long limit) {
        String query =
                """
            delete from outbox where id in (select id from outbox where send_time <= :timeNow limit :limit for update skip locked)
            returning *;
            """;

        return jdbcClient
                .sql(query)
                .param("limit", limit)
                .param("timeNow", UtcDateTimeProvider.now())
                .query(Outbox.class)
                .list();
    }

    @Override
    public void create(Outbox outbox) {
        String query =
                "insert into outbox (link_id, link, chat_id, description, send_time) values (:linkId, :link, :chatId, :description, :sendTime) returning id";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient
                .sql(query)
                .param("linkId", outbox.linkId())
                .param("link", outbox.link())
                .param("chatId", outbox.chatId())
                .param("description", outbox.description())
                .param("sendTime", outbox.sendTime())
                .update(keyHolder);
        outbox.id(keyHolder.getKeyAs(Long.class));
    }
}
