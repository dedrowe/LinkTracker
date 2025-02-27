package backend.academy.scrapper.repository;

import static backend.academy.scrapper.utils.FutureUnwrapper.unwrap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;

import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.repository.linkdata.InMemoryLinkDataRepository;
import backend.academy.shared.exceptions.BaseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InMemoryLinkDataRepositoryTest {

    private InMemoryLinkDataRepository repository;

    @BeforeEach
    public void setUp() {
        List<LinkData> data = new ArrayList<>();
        data.add(new LinkData(1L, 1, 1, new ArrayList<>(), new ArrayList<>()));
        data.add(new LinkData(2L, 2, 1, new ArrayList<>(), new ArrayList<>()));
        data.add(new LinkData(3L, 3, 2, new ArrayList<>(), new ArrayList<>()));
        data.add(new LinkData(4L, 4, 2, new ArrayList<>(), new ArrayList<>()));
        repository = new InMemoryLinkDataRepository(data);
    }

    @Test
    public void getByIdSuccessTest() {
        long id = 1;

        Optional<LinkData> link = unwrap(repository.getById(id));

        assertThat(link.isPresent()).isTrue();
    }

    @Test
    public void getByIdFailTest() {
        long id = -1;

        Optional<LinkData> link = unwrap(repository.getById(id));

        assertThat(link.isEmpty()).isTrue();
    }

    @Test
    public void getAllTest() {

        List<LinkData> data = unwrap(repository.getAll());

        assertThat(data.size()).isEqualTo(4);
    }

    @Test
    public void getByChatIdTest() {
        long chatId = 1;

        List<LinkData> data = unwrap(repository.getByChatId(chatId));

        assertThat(data.size()).isEqualTo(2);
    }

    @Test
    public void getByLinkIdTest() {
        long linkId = 1;

        List<LinkData> data = unwrap(repository.getByLinkId(linkId));

        assertThat(data.size()).isEqualTo(1);
    }

    @Test
    public void getByChatIdLinkIdSuccessTest() {
        long chatId = 1;
        long linkId = 1;

        Optional<LinkData> link = unwrap(repository.getByChatIdLinkId(chatId, linkId));

        assertThat(link.isPresent()).isTrue();
    }

    @Test
    public void getByChatIdLinkIdFailTest() {
        long chatId = 1;
        long linkId = 4;

        Optional<LinkData> link = unwrap(repository.getByChatIdLinkId(chatId, linkId));

        assertThat(link.isEmpty()).isTrue();
    }

    @Test
    public void createTest() {
        LinkData linkData = new LinkData(1L, 5, 3, List.of("work"), List.of("user=user1"));

        unwrap(repository.create(linkData));
        Optional<LinkData> actualLink = unwrap(repository.getByChatIdLinkId(3, 5));
        if (actualLink.isEmpty()) {
            fail("Ссылка не была зарегистрирована");
        }

        assertThat(actualLink.get().tags()).isEqualTo(linkData.tags());
        assertThat(actualLink.get().filters()).isEqualTo(linkData.filters());
    }

    @Test
    public void createDuplicateTest() {
        LinkData linkData = new LinkData(1L, 5, 3, List.of("work"), List.of("user=user1"));

        unwrap(repository.create(linkData));
        Optional<LinkData> actualLink = unwrap(repository.getByChatIdLinkId(3, 5));
        if (actualLink.isEmpty()) {
            fail("Ссылка не была зарегистрирована");
        }

        assertThat(actualLink.get().tags()).isEqualTo(linkData.tags());
        assertThat(actualLink.get().filters()).isEqualTo(linkData.filters());
        assertThatThrownBy(() -> repository.create(linkData)).isInstanceOf(BaseException.class);
    }

    @Test
    public void updateTest() {
        LinkData linkData = new LinkData(1L, 1, 1, List.of("work"), List.of("user=user1"));

        unwrap(repository.update(linkData));
        Optional<LinkData> actualLink = unwrap(repository.getByChatIdLinkId(1, 1));
        if (actualLink.isEmpty()) {
            fail("Ссылка не была зарегистрирована");
        }

        assertThat(actualLink.get().tags()).isEqualTo(linkData.tags());
        assertThat(actualLink.get().filters()).isEqualTo(linkData.filters());
    }

    @Test
    public void deleteByIdTest() {
        long id = 1;

        unwrap(repository.deleteById(id));
        Optional<LinkData> actualLink = unwrap(repository.getById(id));

        assertThat(actualLink.isEmpty()).isTrue();
    }

    @Test
    public void deleteTest() {
        long id = 1;

        Optional<LinkData> linkData = unwrap(repository.getById(id));
        unwrap(repository.delete(linkData.get()));
        Optional<LinkData> actualLink = unwrap(repository.getById(id));

        assertThat(actualLink.isEmpty()).isTrue();
    }
}
