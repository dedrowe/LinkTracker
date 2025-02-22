package backend.academy.scrapper.repository;

import static backend.academy.scrapper.utils.FutureUnwrapper.unwrap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.repository.link.InMemoryLinkRepository;
import backend.academy.shared.exceptions.BaseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InMemoryLinkRepositoryTest {

    private InMemoryLinkRepository repository;

    @BeforeEach
    public void setUp() {
        List<Link> links = new ArrayList<>();
        links.add(new Link(1L, "string", LocalDateTime.now()));
        repository = new InMemoryLinkRepository(links);
    }

    @Test
    public void getAllTest() {
        List<Link> links = unwrap(repository.getAll());

        assertThat(links.size()).isEqualTo(1);
    }

    @Test
    public void getByIdSuccessTest() {
        int id = 1;

        Optional<Link> link = unwrap(repository.getById(id));

        assertThat(link.isPresent()).isTrue();
    }

    @Test
    public void getByIdFailTest() {
        int id = 2;

        Optional<Link> link = unwrap(repository.getById(id));

        assertThat(link.isEmpty()).isTrue();
    }

    @Test
    public void getByLinkSuccessTest() {
        String testLink = "string";

        Optional<Link> link = unwrap(repository.getByLink(testLink));

        assertThat(link.isPresent()).isTrue();
    }

    @Test
    public void getByLinkFailTest() {
        String testLink = "String";

        Optional<Link> link = unwrap(repository.getByLink(testLink));

        assertThat(link.isEmpty()).isTrue();
    }

    @Test
    public void createLinkTest() {
        Link link = new Link("string2");

        unwrap(repository.create(link));
        Optional<Link> actualLink = unwrap(repository.getByLink(link.link()));
        if (actualLink.isEmpty()) {
            fail("Ссылка не была создана");
        }

        assertThat(actualLink.get()).isEqualTo(link);
    }

    @Test
    public void createDuplicateLinkTest() {
        Link link = new Link("string2");

        unwrap(repository.create(link));
        Optional<Link> actualLink = unwrap(repository.getByLink(link.link()));
        if (actualLink.isEmpty()) {
            fail("Ссылка не была создана");
        }

        assertThat(actualLink.get()).isEqualTo(link);
        assertThatThrownBy(() -> unwrap(repository.create(link))).isInstanceOf(BaseException.class);
    }

    @Test
    public void createExistingLinkTest() {
        Link link = new Link("string");

        assertThatThrownBy(() -> unwrap(repository.create(link))).isInstanceOf(BaseException.class);
    }

    @Test
    public void deleteByIdTest() {
        int id = 1;

        unwrap(repository.deleteById(id));
        Optional<Link> actualLink = unwrap(repository.getById(id));

        assertThat(actualLink.isEmpty()).isTrue();
    }

    @Test
    public void deleteTest() {
        int id = 1;

        Optional<Link> link = unwrap(repository.getById(id));
        unwrap(repository.delete(link.get()));
        Optional<Link> actualLink = unwrap(repository.getById(id));

        assertThat(actualLink.isEmpty()).isTrue();
    }
}
