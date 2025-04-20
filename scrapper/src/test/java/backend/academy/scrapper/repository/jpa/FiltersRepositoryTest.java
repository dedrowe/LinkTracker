package backend.academy.scrapper.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.TgChat;
import backend.academy.scrapper.entity.jpa.JpaFilter;
import backend.academy.scrapper.entity.jpa.JpaLinkData;
import backend.academy.scrapper.repository.filters.JpaFiltersRepository;
import java.time.LocalDateTime;
import java.util.List;
import backend.academy.scrapper.utils.UtcDateTimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

public class FiltersRepositoryTest extends AbstractJpaTest {

    private final JpaFiltersRepository repository;

    private final LocalDateTime testTimestamp = UtcDateTimeProvider.of(1741886605);

    @Autowired
    public FiltersRepositoryTest(TestEntityManager entityManager, JpaFiltersRepository repository) {
        super(entityManager);
        this.repository = repository;
    }

    private TgChat tgChat = new TgChat(1);

    private Link link1 = new Link(null, "https://example.com", testTimestamp);
    private Link link2 = new Link(null, "https://example2.com", testTimestamp);

    private JpaLinkData data1 = new JpaLinkData(link1, tgChat);
    private JpaLinkData data2 = new JpaLinkData(link2, tgChat);

    private JpaFilter filter1 = new JpaFilter(data1, "key:value");
    private JpaFilter filter2 = new JpaFilter(data1, "key2:value2");
    private JpaFilter filter3 = new JpaFilter(data2, "key:value");
    private JpaFilter filter4 = new JpaFilter(data2, "key2:value2");

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
        JpaFilter filter1 = new JpaFilter(1L, data1, "key:value");
        JpaFilter filter2 = new JpaFilter(2L, data1, "key2:value2");

        List<JpaFilter> actualResult = repository.getAllByDataId(1L);

        assertThat(actualResult).containsExactly(filter1, filter2);
    }

    @Test
    public void getAllByDataIds() {
        List<JpaFilter> actualResult = repository.getAllByDataIds(List.of(1L, 2L));

        assertThat(actualResult).containsExactly(filter1, filter2, filter3, filter4);
    }

    @Test
    public void getAllByDataIdFailTest() {

        List<JpaFilter> filters = repository.getAllByDataId(-1L);

        assertThat(filters).isEmpty();
    }

    @Test
    public void createTest() {
        long newId = 5L;
        JpaFilter filter = new JpaFilter(null, data1, "test:test");

        repository.create(filter);

        assertThat(filter.id()).isEqualTo(newId);
    }

    @Test
    public void deleteExistingByIdTest() {
        JpaFilter filter = new JpaFilter(1L, data1, "key:value");

        repository.deleteById(filter.id());

        assertThat(entityManager
                        .getEntityManager()
                        .createNativeQuery(
                                "SELECT * FROM filters WHERE data_id = 1 and filter = 'key:value'", JpaFilter.class)
                        .getResultList())
                .isEmpty();
    }

    @Test
    public void deleteAllByDataIdTest() {
        repository.deleteAllByDataId(1);

        assertThat(entityManager
                        .getEntityManager()
                        .createNativeQuery("SELECT * FROM filters WHERE data_id = 1", JpaFilter.class)
                        .getResultList())
                .isEmpty();
        assertThat(entityManager
                        .getEntityManager()
                        .createNativeQuery("SELECT * FROM filters WHERE data_id = 2", JpaFilter.class)
                        .getResultList()
                        .size())
                .isEqualTo(2);
    }
}
