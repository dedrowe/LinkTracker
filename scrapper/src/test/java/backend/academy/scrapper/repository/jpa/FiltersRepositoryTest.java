package backend.academy.scrapper.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.entity.TgChat;
import backend.academy.scrapper.repository.filters.JpaFiltersRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

public class FiltersRepositoryTest extends AbstractJpaTest {

    private final JpaFiltersRepository repository;

    private final LocalDateTime testTimestamp =
            Instant.ofEpochSecond(1741886605).atZone(ZoneOffset.UTC).toLocalDateTime();

    @Autowired
    public FiltersRepositoryTest(TestEntityManager entityManager, JpaFiltersRepository repository) {
        super(entityManager);
        this.repository = repository;
    }

    private TgChat tgChat = new TgChat(1);

    private Link link1 = new Link(null, "https://example.com", testTimestamp);
    private Link link2 = new Link(null, "https://example2.com", testTimestamp);

    private LinkData data1 = new LinkData(link1, tgChat);
    private LinkData data2 = new LinkData(link2, tgChat);

    private Filter filter1 = new Filter(data1, "key:value");
    private Filter filter2 = new Filter(data1, "key2:value2");
    private Filter filter3 = new Filter(data2, "key:value");
    private Filter filter4 = new Filter(data2, "key2:value2");

    @BeforeEach
    public void setUp() {
        entityManager
                .getEntityManager()
                .createNativeQuery("ALTER SEQUENCE filters_id_seq RESTART WITH 1")
                .executeUpdate();
        entityManager
                .getEntityManager()
                .createNativeQuery("ALTER SEQUENCE links_data_id_seq RESTART WITH 1")
                .executeUpdate();
        entityManager
                .getEntityManager()
                .createNativeQuery("ALTER SEQUENCE links_id_seq RESTART WITH 1")
                .executeUpdate();
        entityManager
                .getEntityManager()
                .createNativeQuery("ALTER SEQUENCE tg_chats_id_seq RESTART WITH 1")
                .executeUpdate();

        entityManager.persist(tgChat);
        entityManager.persist(link1);
        entityManager.persist(link2);
        entityManager.persist(data1);
        entityManager.persist(data2);
        entityManager.persist(filter1);
        entityManager.persist(filter2);
        entityManager.persist(filter3);
        entityManager.persist(filter4);
        entityManager.flush();
    }

    @Test
    public void getAllByDataIdTest() {
        Filter filter1 = new Filter(1L, data1, "key:value");
        Filter filter2 = new Filter(2L, data1, "key2:value2");

        List<Filter> actualResult = repository.getAllByDataIdSync(1L);

        assertThat(actualResult).containsExactly(filter1, filter2);
    }

    @Test
    public void getAllByDataIdFailTest() {

        List<Filter> filters = repository.getAllByDataIdSync(-1L);

        assertThat(filters).isEmpty();
    }

    @Test
    public void createTest() {
        long newId = 5L;
        Filter filter = new Filter(null, data1, "test:test");

        repository.createSync(filter);

        assertThat(filter.id()).isEqualTo(newId);
    }

    @Test
    public void deleteExistingByIdTest() {
        Filter filter = new Filter(1L, data1, "key:value");

        repository.deleteByIdSync(filter.id());

        assertThat(entityManager
                        .getEntityManager()
                        .createNativeQuery(
                                "SELECT * FROM filters WHERE data_id = 1 and filter = 'key:value'", Filter.class)
                        .getResultList())
                .isEmpty();
    }

    @Test
    public void deleteAllByDataIdTest() {
        repository.deleteAllByDataIdSync(1);

        assertThat(entityManager
                        .getEntityManager()
                        .createNativeQuery("SELECT * FROM filters WHERE data_id = 1", Filter.class)
                        .getResultList())
                .isEmpty();
        assertThat(entityManager
                        .getEntityManager()
                        .createNativeQuery("SELECT * FROM filters WHERE data_id = 2", Filter.class)
                        .getResultList()
                        .size())
                .isEqualTo(2);
    }
}
