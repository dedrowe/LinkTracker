package backend.academy.scrapper.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.repository.link.JpaLinkRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

public class LinkRepositoryTest extends AbstractJpaTest {

    private final JpaLinkRepository repository;

    private final LocalDateTime testTimestamp =
            Instant.ofEpochSecond(1741886605).atZone(ZoneOffset.UTC).toLocalDateTime();

    private final Duration linksCheckInterval = Duration.ofSeconds(60);

    @Autowired
    public LinkRepositoryTest(TestEntityManager entityManager, JpaLinkRepository repository) {
        super(entityManager);
        this.repository = repository;
    }

    @BeforeEach
    public void setUp() {
        entityManager
                .getEntityManager()
                .createNativeQuery("ALTER SEQUENCE links_id_seq RESTART WITH 1")
                .executeUpdate();
        entityManager.persist(new Link(null, "https://example.com", testTimestamp));
        entityManager.persist(new Link(null, "https://example2.com", testTimestamp, true));
        entityManager.flush();
    }

    @Test
    public void getAllTest() {
        Link expectedResult = new Link(1L, "https://example.com", testTimestamp);

        List<Link> actualResult = repository.getAllSync();

        assertThat(actualResult.size()).isEqualTo(1);
        assertThat(actualResult.getFirst().id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.getFirst().link()).isEqualTo(expectedResult.link());
        assertThat(actualResult.getFirst().lastUpdate()).isEqualTo(expectedResult.lastUpdate());
    }

    @Test
    public void getAllWithSkipLimitTest() {
        long skip = 1L;
        long limit = 3L;
        Link link1 = new Link(null, "https://example3.com", testTimestamp);
        Link link2 = new Link(null, "https://example4.com", testTimestamp);
        Link link3 = new Link(null, "https://example5.com", testTimestamp);
        entityManager.persist(link1);
        entityManager.persist(link2);
        entityManager.persist(link3);
        entityManager.flush();

        List<Link> actualResult = repository.getAllSync(skip, limit);

        assertThat(actualResult).containsExactly(link1, link2, link3);
    }

    @Test
    public void getAllNotCheckedTest() {
        Link expectedResult =
                new Link(null, "https://example3.com", testTimestamp.minus(linksCheckInterval.plusSeconds(30)));
        entityManager.persistAndFlush(expectedResult);

        List<Link> actualResult = repository.getAllNotCheckedSync(10L, testTimestamp, linksCheckInterval.getSeconds());

        assertThat(actualResult).containsExactly(expectedResult);
    }

    @Test
    public void getByIdTest() {
        long id = 1L;
        Link expectedResult = new Link(id, "https://example.com", testTimestamp);

        Link actualResult = repository.getByIdSync(id).get();

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.link()).isEqualTo(expectedResult.link());
        assertThat(actualResult.lastUpdate()).isEqualTo(expectedResult.lastUpdate());
    }

    @Test
    public void getDeletedByIdTest() {
        Optional<Link> actualResult = repository.getByIdSync(2);

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByIdFailTest() {
        Optional<Link> actualResult = repository.getByIdSync(-1);

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByLinkTest() {
        String link = "https://example.com";
        Link expectedResult = new Link(1L, link, testTimestamp);

        Link actualResult = repository.getByLinkSync(link).get();

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.link()).isEqualTo(expectedResult.link());
        assertThat(actualResult.lastUpdate()).isEqualTo(expectedResult.lastUpdate());
    }

    @Test
    public void getDeletedByLinkTest() {
        Optional<Link> actualResult = repository.getByLinkSync("https://example2.com");

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void createNewTest() {
        Link expectedResult = new Link(null, "https://example3.com", testTimestamp);

        repository.createSync(expectedResult);
        Link actualResult = entityManager.find(Link.class, expectedResult.id());

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.link()).isEqualTo(expectedResult.link());
        assertThat(actualResult.lastUpdate()).isEqualTo(expectedResult.lastUpdate());
    }

    @Test
    public void createDeletedTest() {
        long expectedId = 2L;
        Link expectedResult = new Link(null, "https://example2.com", testTimestamp);

        repository.createSync(expectedResult);
        Link actualResult = entityManager.find(Link.class, expectedId);

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.link()).isEqualTo(expectedResult.link());
        assertThat(actualResult.lastUpdate()).isEqualTo(expectedResult.lastUpdate());
    }

    @Test
    public void createExistingTest() {
        long expectedId = 1L;
        Link expectedResult = new Link(null, "https://example.com", testTimestamp);

        repository.createSync(expectedResult);

        assertThat(expectedResult.id()).isEqualTo(expectedId);
    }

    @Test
    public void updateTest() {
        Link expectedResult = new Link(1L, "https://example3.com", testTimestamp.plusDays(3));

        repository.updateSync(expectedResult.link(), expectedResult.lastUpdate(), expectedResult.id());
        entityManager.clear();
        Link actualResult = entityManager.find(Link.class, expectedResult.id());

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.link()).isEqualTo(expectedResult.link());
        assertThat(actualResult.lastUpdate()).isEqualTo(expectedResult.lastUpdate());
    }

    @Test
    public void deleteByIdTest() {
        long id = 1L;

        repository.deleteByIdSync(id);
        entityManager.clear();
        Link actualResult = entityManager.find(Link.class, id);

        assertThat(actualResult.deleted()).isTrue();
    }

    @Test
    public void deleteAlreadyDeletedByIdTest() {
        long id = 2L;

        repository.deleteByIdSync(id);

        Link actualResult = entityManager.find(Link.class, id);

        assertThat(actualResult.deleted()).isTrue();
    }

    @Test
    public void deleteTest() {
        Link link = new Link(1L, "https://example.com", testTimestamp);

        repository.deleteSync(link.link());
        entityManager.clear();
        Link actualResult = entityManager.find(Link.class, link.id());

        assertThat(actualResult.deleted()).isTrue();
    }

    @Test
    public void deleteAlreadyDeletedTest() {
        Link link = new Link(2L, "https://example2.com", testTimestamp);

        repository.deleteSync(link.link());
        Link actualResult = entityManager.find(Link.class, link.id());

        assertThat(actualResult.deleted()).isTrue();
    }
}
