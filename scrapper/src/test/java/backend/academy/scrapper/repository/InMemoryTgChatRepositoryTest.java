package backend.academy.scrapper.repository;

import static backend.academy.scrapper.utils.FutureUnwrapper.unwrap;
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

        Optional<TgChat> tgChat = unwrap(repository.getById(id));

        assertThat(tgChat.isPresent()).isTrue();
    }

    @Test
    public void getByIdFailTest() {
        int id = 2;

        Optional<TgChat> tgChat = unwrap(repository.getById(id));

        assertThat(tgChat.isEmpty()).isTrue();
    }

    @Test
    public void getByChatIdSuccessTest() {
        long chatId = 123;

        Optional<TgChat> tgChat = unwrap(repository.getByChatId(chatId));

        assertThat(tgChat.isPresent()).isTrue();
    }

    @Test
    public void getByChatIdFailTest() {
        long chatId = 1234;

        Optional<TgChat> tgChat = unwrap(repository.getByChatId(chatId));

        assertThat(tgChat.isEmpty()).isTrue();
    }

    @Test
    public void createTest() {
        TgChat tgChat = new TgChat(1234);

        unwrap(repository.create(tgChat));
        Optional<TgChat> actualChat = unwrap(repository.getByChatId(tgChat.chatId()));
        if (actualChat.isEmpty()) {
            fail("Чат не был создан");
        }

        assertThat(actualChat.get()).isEqualTo(tgChat);
    }

    @Test
    public void createExistingTest() {
        TgChat tgChat = new TgChat(123);

        assertThatThrownBy(() -> unwrap(repository.create(tgChat))).isInstanceOf(BaseException.class);
    }

    @Test
    public void deleteByIdTest() {
        int id = 1;

        unwrap(repository.deleteById(id));
        Optional<TgChat> actualChat = unwrap(repository.getById(id));

        assertThat(actualChat.isEmpty()).isTrue();
    }

    @Test
    public void deleteTest() {
        int id = 1;

        Optional<TgChat> tgChat = unwrap(repository.getById(id));
        unwrap(repository.delete(tgChat.get()));
        Optional<TgChat> actualChat = unwrap(repository.getById(id));

        assertThat(actualChat.isEmpty()).isTrue();
    }
}
