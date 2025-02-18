package backend.academy.scrapper.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;

import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.exceptionHandling.exceptions.ScrapperBaseException;
import backend.academy.scrapper.repository.linkdata.InMemoryLinkDataRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class InMemoryLinkDataRepositoryTest {

    private InMemoryLinkDataRepository repository;

    @BeforeEach
    public void setUp() {
        List<LinkData> data = new ArrayList<>();
        data.add(new LinkData(1L, 1, 1, new ArrayList<>(), new ArrayList<>(), LocalDateTime.now()));
        data.add(new LinkData(2L, 2, 1, new ArrayList<>(), new ArrayList<>(), LocalDateTime.now()));
        data.add(new LinkData(3L, 3, 2, new ArrayList<>(), new ArrayList<>(), LocalDateTime.now()));
        data.add(new LinkData(4L, 4, 2, new ArrayList<>(), new ArrayList<>(), LocalDateTime.now()));
        repository = new InMemoryLinkDataRepository(data);
    }

    @Test
    public void getByIdSuccessTest() {
        int id = 1;

        Optional<LinkData> link = repository.getById(id);

        assertThat(link.isPresent()).isTrue();
    }

    @Test
    public void getByIdFailTest() {
        int id = -1;

        Optional<LinkData> link = repository.getById(id);

        assertThat(link.isEmpty()).isTrue();
    }

    @Test
    public void getAllTest() {

        List<LinkData> data = repository.getAll();

        assertThat(data.size()).isEqualTo(4);
    }

    @Test
    public void getByChatIdTest() {
        int chatId = 1;

        List<LinkData> data = repository.getByChatId(chatId);

        assertThat(data.size()).isEqualTo(2);
    }

    @ParameterizedTest
    @CsvSource({
        "1, 0, 1, 1",
        "1, 0, 2, 2",
        "1, 0, 5, 2",
    })
    public void getByChatIdTest(int chatId, int offset, int limit, int expectedSize) {

        List<LinkData> data = repository.getByChatId(chatId, offset, limit);

        assertThat(data.size()).isEqualTo(expectedSize);
    }

    @Test
    public void getByLinkIdTest() {
        int linkId = 1;

        List<LinkData> data = repository.getByLinkId(linkId);

        assertThat(data.size()).isEqualTo(1);
    }

    @Test
    public void getByChatIdLinkIdSuccessTest() {
        int chatId = 1;
        int linkId = 1;

        Optional<LinkData> link = repository.getByChatIdLinkId(chatId, linkId);

        assertThat(link.isPresent()).isTrue();
    }

    @Test
    public void getByChatIdLinkIdFailTest() {
        int chatId = 1;
        int linkId = 4;

        Optional<LinkData> link = repository.getByChatIdLinkId(chatId, linkId);

        assertThat(link.isEmpty()).isTrue();
    }

    @Test
    public void createTest() {
        LinkData linkData = new LinkData(5, 3, List.of("work"), List.of("user=user1"));

        repository.create(linkData);
        Optional<LinkData> actualLink = repository.getByChatIdLinkId(3, 5);
        if (actualLink.isEmpty()) {
            fail("Ссылка не была зарегистрирована");
        }

        assertThat(actualLink.get().tags()).isEqualTo(linkData.tags());
        assertThat(actualLink.get().filters()).isEqualTo(linkData.filters());
    }

    @Test
    public void createDuplicateTest() {
        LinkData linkData = new LinkData(5, 3, List.of("work"), List.of("user=user1"));

        repository.create(linkData);
        Optional<LinkData> actualLink = repository.getByChatIdLinkId(3, 5);
        if (actualLink.isEmpty()) {
            fail("Ссылка не была зарегистрирована");
        }

        assertThat(actualLink.get().tags()).isEqualTo(linkData.tags());
        assertThat(actualLink.get().filters()).isEqualTo(linkData.filters());
        assertThatThrownBy(() -> repository.create(linkData)).isInstanceOf(ScrapperBaseException.class);
    }

    @Test
    public void updateTest() {
        LinkData linkData = new LinkData(1, 1, List.of("work"), List.of("user=user1"));

        repository.update(linkData);
        Optional<LinkData> actualLink = repository.getByChatIdLinkId(1, 1);
        if (actualLink.isEmpty()) {
            fail("Ссылка не была зарегистрирована");
        }

        assertThat(actualLink.get().tags()).isEqualTo(linkData.tags());
        assertThat(actualLink.get().filters()).isEqualTo(linkData.filters());
    }

    @Test
    public void deleteByIdTest() {
        int id = 1;

        repository.deleteById(id);
        Optional<LinkData> actualLink = repository.getById(id);

        assertThat(actualLink.isEmpty()).isTrue();
    }

    @Test
    public void deleteTest() {
        int id = 1;

        Optional<LinkData> linkData = repository.getById(id);
        repository.delete(linkData.get());
        Optional<LinkData> actualLink = repository.getById(id);

        assertThat(actualLink.isEmpty()).isTrue();
    }
}
