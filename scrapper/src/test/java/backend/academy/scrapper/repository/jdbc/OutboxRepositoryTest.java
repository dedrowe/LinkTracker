package backend.academy.scrapper.repository.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.entity.Outbox;
import backend.academy.scrapper.repository.outbox.JdbcOutboxRepository;
import backend.academy.scrapper.utils.UtcDateTimeProvider;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

public class OutboxRepositoryTest extends AbstractJdbcTest {

    private final JdbcOutboxRepository repository;

    private final LocalDateTime testTime = UtcDateTimeProvider.now();

    @Autowired
    public OutboxRepositoryTest(JdbcClient client) {
        super(client);
        this.repository = new JdbcOutboxRepository(client);
    }

    @BeforeEach
    public void setUp() {
        client.sql("alter sequence outbox_id_seq restart with 1").update();

        client.sql(
                        "insert into outbox (link, chat_id, description, send_time) VALUES ('https://example.com', 1, 'test description', :testTime)")
                .param("testTime", testTime.plusHours(1))
                .update();
        client.sql(
                        "insert into outbox (link, chat_id, description, send_time) VALUES ('https://example.com', 2, 'test description', :testTime)")
                .param("testTime", testTime.minusHours(1))
                .update();
    }

    @Test
    public void getAllWithDeletionTest() {
        Outbox expectedResult = new Outbox(2L, "https://example.com", 2, "test description", testTime.minusHours(1));

        List<Outbox> actualResult = repository.getAllWithDeletion(10);

        assertThat(actualResult).containsExactly(expectedResult);
        assertThat(client.sql("select count(*) from outbox").query(Long.class).single())
                .isEqualTo(1);
    }

    @Test
    @SuppressWarnings("JavaTimeDefaultTimeZone")
    public void createTest() {
        long expectedId = 3;
        Outbox outbox = new Outbox(1, "https://example.com", 3, "test description", LocalDateTime.now());

        repository.create(outbox);

        assertThat(outbox.id()).isEqualTo(expectedId);
    }
}
