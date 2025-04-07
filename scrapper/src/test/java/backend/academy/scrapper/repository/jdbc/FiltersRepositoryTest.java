package backend.academy.scrapper.repository.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.entity.jdbc.JdbcFilter;
import backend.academy.scrapper.repository.filters.JdbcFiltersRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

public class FiltersRepositoryTest extends AbstractJdbcTest {

    private final JdbcFiltersRepository repository;

    @Autowired
    public FiltersRepositoryTest(JdbcClient client) {
        super(client);
        repository = new JdbcFiltersRepository(client);
    }

    @BeforeEach
    public void setUp() {
        client.sql("ALTER SEQUENCE filters_id_seq RESTART WITH 1").update();
        client.sql("ALTER SEQUENCE links_data_id_seq RESTART WITH 1").update();
        client.sql("ALTER SEQUENCE links_id_seq RESTART WITH 1").update();
        client.sql("ALTER SEQUENCE tg_chats_id_seq RESTART WITH 1").update();

        client.sql("INSERT INTO tg_chats (chat_id) VALUES (1)").update();
        client.sql("INSERT INTO links (link, last_update) VALUES ('https://example.com', '2025-03-13 17:23:25')")
                .update();
        client.sql("INSERT INTO links (link, last_update) VALUES ('https://example2.com', '2025-03-13 17:23:25')")
                .update();
        client.sql("INSERT INTO links_data (link_id, chat_id) VALUES (1, 1)").update();
        client.sql("INSERT INTO links_data (link_id, chat_id) VALUES (2, 1)").update();
        client.sql("INSERT INTO filters (data_id, filter) VALUES (1, 'key:value')")
                .update();
        client.sql("INSERT INTO filters (data_id, filter) VALUES (1, 'key2:value2')")
                .update();
        client.sql("INSERT INTO filters (data_id, filter) VALUES (2, 'key:value')")
                .update();

        client.sql("INSERT INTO filters (data_id, filter) VALUES (2, 'key2:value2')")
                .update();
    }

    @Test
    public void getAllByDataIdTest() {
        JdbcFilter filter1 = new JdbcFilter(1L, 1L, "key:value");
        JdbcFilter filter2 = new JdbcFilter(2L, 1L, "key2:value2");

        List<JdbcFilter> filters = repository.getAllByDataId(1);

        assertThat(filters).containsExactly(filter1, filter2);
    }

    @Test
    public void getAllByDataIdFailTest() {

        List<JdbcFilter> filters = repository.getAllByDataId(-1);

        assertThat(filters).isEmpty();
    }

    @Test
    public void createTest() {
        long newId = 5L;
        JdbcFilter filter = new JdbcFilter(null, 1L, "test:test");

        repository.create(filter);

        assertThat(filter.id()).isEqualTo(newId);
    }

    @Test
    public void deleteExistingByIdTest() {
        JdbcFilter filter = new JdbcFilter(1L, 1L, "key:value");

        repository.deleteById(filter.id());

        assertThat(client.sql("SELECT * FROM filters WHERE data_id = 1 and filter = 'key:value'")
                        .query(JdbcFilter.class)
                        .optional())
                .isEmpty();
    }

    @Test
    public void deleteAllByDataIdTest() {

        repository.deleteAllByDataId(1L);

        assertThat(client.sql("SELECT * FROM filters WHERE data_id = 1")
                        .query(JdbcFilter.class)
                        .list())
                .isEmpty();
        assertThat(client.sql("SELECT * FROM filters WHERE data_id = 2")
                        .query(JdbcFilter.class)
                        .list()
                        .size())
                .isEqualTo(2);
    }
}
