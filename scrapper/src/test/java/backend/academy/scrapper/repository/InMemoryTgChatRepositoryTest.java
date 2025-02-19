package backend.academy.scrapper.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;

import backend.academy.scrapper.entity.TgChat;
import backend.academy.scrapper.repository.tgchat.InMemoryTgChatRepository;
import backend.academy.shared.exceptions.BaseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class InMemoryTgChatRepositoryTest {

    private InMemoryTgChatRepository repository;

    @BeforeEach
    public void setUp() {
        List<TgChat> data = new ArrayList<>();
        data.add(new TgChat(1L, 123));
        repository = new InMemoryTgChatRepository(data);
    }

    @Test
    public void getByIdSuccessTest() {
        int id = 1;

        Optional<TgChat> tgChat = repository.getById(id);

        assertThat(tgChat.isPresent()).isTrue();
    }

    @Test
    public void getByIdFailTest() {
        int id = 2;

        Optional<TgChat> tgChat = repository.getById(id);

        assertThat(tgChat.isEmpty()).isTrue();
    }

    @Test
    public void getByChatIdSuccessTest() {
        long chatId = 123;

        Optional<TgChat> tgChat = repository.getByChatId(chatId);

        assertThat(tgChat.isPresent()).isTrue();
    }

    @Test
    public void getByChatIdFailTest() {
        long chatId = 1234;

        Optional<TgChat> tgChat = repository.getByChatId(chatId);

        assertThat(tgChat.isEmpty()).isTrue();
    }

    @Test
    public void createTest() {
        TgChat tgChat = new TgChat(1234);

        repository.create(tgChat);
        Optional<TgChat> actualChat = repository.getByChatId(tgChat.chatId());
        if (actualChat.isEmpty()) {
            fail("Чат не был создан");
        }

        assertThat(actualChat.get()).isEqualTo(tgChat);
    }

    @Test
    public void createExistingTest() {
        TgChat tgChat = new TgChat(123);

        assertThatThrownBy(() -> repository.create(tgChat)).isInstanceOf(BaseException.class);
    }

    @Test
    public void deleteByIdTest() {
        int id = 1;

        repository.deleteById(id);
        Optional<TgChat> actualChat = repository.getById(id);

        assertThat(actualChat.isEmpty()).isTrue();
    }

    @Test
    public void deleteTest() {
        int id = 1;

        Optional<TgChat> tgChat = repository.getById(id);
        repository.delete(tgChat.get());
        Optional<TgChat> actualChat = repository.getById(id);

        assertThat(actualChat.isEmpty()).isTrue();
    }
}
