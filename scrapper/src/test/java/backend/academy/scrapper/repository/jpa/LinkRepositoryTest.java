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

        List<Link> actualResult = repository.getAll();

        assertThat(actualResult.size()).isEqualTo(1);
        assertThat(actualResult.getFirst().id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.getFirst().link()).isEqualTo(expectedResult.link());
        assertThat(actualResult.getFirst().lastUpdate()).isEqualTo(expectedResult.lastUpdate());
    }

    @Test
    public void getAllByIdsTest() {
        Link expectedResult = new Link(1L, "https://example.com", testTimestamp);

        List<Link> actualResult = repository.getAllByIds(List.of(expectedResult.id(), 2L));

        assertThat(actualResult.size()).isEqualTo(1);
        assertThat(actualResult.getFirst().id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.getFirst().link()).isEqualTo(expectedResult.link());
        assertThat(actualResult.getFirst().lastUpdate()).isEqualTo(expectedResult.lastUpdate());
    }

    @Test
    public void getNotCheckedTest() {
        Link expectedResult =
                new Link(null, "https://example3.com", testTimestamp.minus(linksCheckInterval.plusSeconds(30)));
        entityManager.persistAndFlush(expectedResult);

        List<Link> actualResult = repository.getNotChecked(10L, testTimestamp, linksCheckInterval.getSeconds());
        entityManager.refresh(actualResult.getFirst());

        assertThat(actualResult).containsExactly(expectedResult);
        assertThat(actualResult.getFirst().checking()).isEqualTo(true);
    }

    @Test
    public void getByIdTest() {
        long id = 1L;
        Link expectedResult = new Link(id, "https://example.com", testTimestamp);

        Link actualResult = repository.getById(id).get();

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.link()).isEqualTo(expectedResult.link());
        assertThat(actualResult.lastUpdate()).isEqualTo(expectedResult.lastUpdate());
    }

    @Test
    public void getDeletedByIdTest() {
        Optional<Link> actualResult = repository.getById(2);

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByIdFailTest() {
        Optional<Link> actualResult = repository.getById(-1);

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByLinkTest() {
        String link = "https://example.com";
        Link expectedResult = new Link(1L, link, testTimestamp);

        Link actualResult = repository.getByLink(link).get();

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.link()).isEqualTo(expectedResult.link());
        assertThat(actualResult.lastUpdate()).isEqualTo(expectedResult.lastUpdate());
    }

    @Test
    public void getDeletedByLinkTest() {
        Optional<Link> actualResult = repository.getByLink("https://example2.com");

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void createNewTest() {
        Link expectedResult = new Link(null, "https://example3.com", testTimestamp);

        repository.create(expectedResult);
        Link actualResult = entityManager.find(Link.class, expectedResult.id());

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.link()).isEqualTo(expectedResult.link());
        assertThat(actualResult.lastUpdate()).isEqualTo(expectedResult.lastUpdate());
    }

    @Test
    public void createDeletedTest() {
        long expectedId = 2L;
        Link expectedResult = new Link(null, "https://example2.com", testTimestamp);

        repository.create(expectedResult);
        Link actualResult = entityManager.find(Link.class, expectedId);

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.link()).isEqualTo(expectedResult.link());
        assertThat(actualResult.lastUpdate()).isEqualTo(expectedResult.lastUpdate());
    }

    @Test
    public void createExistingTest() {
        long expectedId = 1L;
        Link expectedResult = new Link(null, "https://example.com", testTimestamp);

        repository.create(expectedResult);

        assertThat(expectedResult.id()).isEqualTo(expectedId);
    }

    @Test
    public void updateTest() {
        Link expectedResult = new Link(1L, "https://example3.com", testTimestamp.plusDays(3));

        repository.update(
                expectedResult.link(), expectedResult.lastUpdate(), expectedResult.id(), expectedResult.checking());
        entityManager.clear();
        Link actualResult = entityManager.find(Link.class, expectedResult.id());

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.link()).isEqualTo(expectedResult.link());
        assertThat(actualResult.lastUpdate()).isEqualTo(expectedResult.lastUpdate());
    }

    @Test
    public void deleteByIdTest() {
        long id = 1L;

        repository.deleteById(id);
        entityManager.clear();
        Link actualResult = entityManager.find(Link.class, id);

        assertThat(actualResult.deleted()).isTrue();
    }

    @Test
    public void deleteAlreadyDeletedByIdTest() {
        long id = 2L;

        repository.deleteById(id);

        Link actualResult = entityManager.find(Link.class, id);

        assertThat(actualResult.deleted()).isTrue();
    }

    @Test
    public void deleteTest() {
        Link link = new Link(1L, "https://example.com", testTimestamp);

        repository.delete(link.link());
        entityManager.clear();
        Link actualResult = entityManager.find(Link.class, link.id());

        assertThat(actualResult.deleted()).isTrue();
    }

    @Test
    public void deleteAlreadyDeletedTest() {
        Link link = new Link(2L, "https://example2.com", testTimestamp);

        repository.delete(link.link());
        Link actualResult = entityManager.find(Link.class, link.id());

        assertThat(actualResult.deleted()).isTrue();
    }
}
