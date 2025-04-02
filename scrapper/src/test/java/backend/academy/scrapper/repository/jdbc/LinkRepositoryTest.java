package backend.academy.scrapper.repository.jdbc;

import static backend.academy.scrapper.utils.FutureUnwrapper.unwrap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.exceptionHandling.exceptions.LinkException;
import backend.academy.scrapper.repository.link.JdbcLinkRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

public class LinkRepositoryTest extends AbstractJdbcTest {

    private final JdbcLinkRepository repository;

    private final Duration linksCheckInterval = Duration.ofSeconds(60);

    @Autowired
    public LinkRepositoryTest(JdbcClient client) {
        super(client);
        repository = new JdbcLinkRepository(client);
    }

    private final LocalDateTime testTimestamp =
            Instant.ofEpochSecond(1741886605).atZone(ZoneOffset.UTC).toLocalDateTime();

    @BeforeEach
    void setUp() {
        client.sql("ALTER SEQUENCE links_id_seq RESTART WITH 1").update();
        client.sql(
                        "INSERT INTO links (link, last_update, deleted) VALUES ('https://example.com', '2025-03-13 17:23:25', false)")
                .update();
        client.sql(
                        "INSERT INTO links (link, last_update, deleted) VALUES ('https://example2.com', '2025-03-13 17:23:25', true)")
                .update();
    }

    @Test
    public void getAllTest() {
        Link expectedResult = new Link(1L, "https://example.com", testTimestamp);

        List<Link> actualResult = unwrap(repository.getAll());

        assertThat(actualResult.size()).isEqualTo(1);
        assertThat(actualResult.getFirst().id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.getFirst().link()).isEqualTo(expectedResult.link());
        assertThat(actualResult.getFirst().lastUpdate()).isEqualTo(expectedResult.lastUpdate());
    }

    @Test
    public void getAllWithSkipLimitTest() {
        long skip = 1L;
        long limit = 3L;
        client.sql("INSERT INTO links (link, last_update) VALUES ('https://example3.com', '2025-03-13 17:23:25')")
                .update();
        client.sql("INSERT INTO links (link, last_update) VALUES ('https://example4.com', '2025-03-13 17:23:25')")
                .update();
        client.sql("INSERT INTO links (link, last_update) VALUES ('https://example5.com', '2025-03-13 17:23:25')")
                .update();
        Link link1 = new Link(3L, "https://example3.com", testTimestamp);
        Link link2 = new Link(4L, "https://example4.com", testTimestamp);
        Link link3 = new Link(5L, "https://example5.com", testTimestamp);

        List<Link> actualResult = unwrap(repository.getAll(skip, limit));

        assertThat(actualResult).containsExactly(link1, link2, link3);
    }

    @Test
    public void getAllNotCheckedTest() {
        Link expectedResult =
                new Link(3L, "https://example3.com", testTimestamp.minus(linksCheckInterval.plusSeconds(30)));
        client.sql("INSERT INTO links (link, last_update) VALUES ('https://example3.com', :lastUpdate)")
                .param("lastUpdate", expectedResult.lastUpdate())
                .update();

        List<Link> actualResult =
                unwrap(repository.getAllNotChecked(10L, testTimestamp, linksCheckInterval.getSeconds()));

        assertThat(actualResult).containsExactly(expectedResult);
    }

    @Test
    public void getByIdTest() {
        long id = 1L;
        Link expectedResult = new Link(id, "https://example.com", testTimestamp);

        Link actualResult = unwrap(repository.getById(id)).get();

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.link()).isEqualTo(expectedResult.link());
        assertThat(actualResult.lastUpdate()).isEqualTo(expectedResult.lastUpdate());
    }

    @Test
    public void getDeletedByIdTest() {
        Optional<Link> actualResult = unwrap(repository.getById(2));

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByIdFailTest() {
        Optional<Link> actualResult = unwrap(repository.getById(-1));

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByLinkTest() {
        String link = "https://example.com";
        Link expectedResult = new Link(1L, "https://example.com", testTimestamp);

        Link actualResult = unwrap(repository.getByLink(link)).get();

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.link()).isEqualTo(expectedResult.link());
        assertThat(actualResult.lastUpdate()).isEqualTo(expectedResult.lastUpdate());
    }

    @Test
    public void getDeletedByLinkTest() {
        Optional<Link> actualResult = unwrap(repository.getByLink("https://example2.com"));

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void createNewTest() {
        Link expectedResult = new Link(3L, "https://example3.com", testTimestamp);

        unwrap(repository.create(expectedResult));
        Link actualResult = client.sql("SELECT * FROM links WHERE id = ?")
                .param(expectedResult.id())
                .query(Link.class)
                .single();

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.link()).isEqualTo(expectedResult.link());
        assertThat(actualResult.lastUpdate()).isEqualTo(expectedResult.lastUpdate());
    }

    @Test
    public void createDeletedTest() {
        Link expectedResult = new Link(2L, "https://example2.com", testTimestamp);

        unwrap(repository.create(expectedResult));
        Link actualResult = client.sql("SELECT * FROM links WHERE id = ?")
                .param(expectedResult.id())
                .query(Link.class)
                .single();

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.link()).isEqualTo(expectedResult.link());
        assertThat(actualResult.lastUpdate()).isEqualTo(expectedResult.lastUpdate());
    }

    @Test
    public void createExistingTest() {
        Link expectedResult = new Link(1L, "https://example.com", testTimestamp);

        assertThatThrownBy(() -> unwrap(repository.create(expectedResult))).isInstanceOf(LinkException.class);
    }

    @Test
    public void updateTest() {
        Link expectedResult = new Link(1L, "https://example3.com", testTimestamp.plusDays(3));

        unwrap(repository.update(expectedResult));

        Link actualResult = client.sql("SELECT * FROM links WHERE id = ?")
                .param(1)
                .query(Link.class)
                .single();

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.link()).isEqualTo(expectedResult.link());
        assertThat(actualResult.lastUpdate()).isEqualTo(expectedResult.lastUpdate());
    }

    @Test
    public void deleteByIdTest() {
        long id = 1L;

        unwrap(repository.deleteById(id));

        assertThat(client.sql("SELECT deleted FROM links WHERE id = ?")
                        .param(id)
                        .query(Boolean.class)
                        .single())
                .isTrue();
    }

    @Test
    public void deleteAlreadyDeletedByIdTest() {
        long id = 2L;

        unwrap(repository.deleteById(id));

        assertThat(client.sql("SELECT deleted FROM links WHERE id = ?")
                        .param(id)
                        .query(Boolean.class)
                        .single())
                .isTrue();
    }

    @Test
    public void deleteTest() {
        Link link = new Link(1L, "https://example.com", testTimestamp);

        unwrap(repository.deleteLink(link));

        assertThat(client.sql("SELECT deleted FROM links WHERE id = ?")
                        .param(link.id())
                        .query(Boolean.class)
                        .single())
                .isTrue();
    }

    @Test
    public void deleteAlreadyDeletedTest() {
        Link link = new Link(2L, "https://example2.com", testTimestamp);

        unwrap(repository.deleteLink(link));

        assertThat(client.sql("SELECT deleted FROM links WHERE link = ?")
                        .param(link.link())
                        .query(Boolean.class)
                        .single())
                .isTrue();
    }
}
