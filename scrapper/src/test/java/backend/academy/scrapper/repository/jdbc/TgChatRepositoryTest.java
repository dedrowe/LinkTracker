package backend.academy.scrapper.repository.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.entity.TgChat;
import backend.academy.scrapper.repository.tgchat.JdbcTgChatRepository;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

public class TgChatRepositoryTest extends AbstractJdbcTest {

    private final JdbcTgChatRepository repository;

    @Autowired
    public TgChatRepositoryTest(JdbcClient client) {
        super(client);
        repository = new JdbcTgChatRepository(client);
    }

    @BeforeEach
    void setUp() {
        client.sql("ALTER SEQUENCE tg_chats_id_seq RESTART WITH 1").update();
        client.sql("INSERT INTO tg_chats (chat_id, deleted) VALUES (1, false)").update();
        client.sql("INSERT INTO tg_chats (chat_id, deleted) VALUES (2, true)").update();
    }

    @Test
    public void getByIdTest() {
        TgChat expectedResult = new TgChat(1L, 1L);

        TgChat actualResult = repository.getById(1).get();

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.chatId()).isEqualTo(expectedResult.chatId());
        assertThat(actualResult.deleted()).isEqualTo(expectedResult.deleted());
    }

    @Test
    public void getDeletedByIdTest() {
        Optional<TgChat> actualResult = repository.getById(2);

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByIdFailTest() {
        Optional<TgChat> actualResult = repository.getById(-1);

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByChatIdTest() {
        TgChat expectedResult = new TgChat(1L, 1L);

        TgChat actualResult = repository.getByChatId(1).get();

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.chatId()).isEqualTo(expectedResult.chatId());
        assertThat(actualResult.deleted()).isEqualTo(expectedResult.deleted());
    }

    @Test
    public void getDeletedByChatIdTest() {
        Optional<TgChat> actualResult = repository.getByChatId(2);

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void createNewChatTest() {
        TgChat expectedResult = new TgChat(3L, 3L);

        repository.create(new TgChat(expectedResult.chatId()));
        TgChat actualResult = client.sql("SELECT * FROM tg_chats where id = ?")
                .param(expectedResult.id())
                .query(TgChat.class)
                .single();

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.chatId()).isEqualTo(expectedResult.chatId());
        assertThat(actualResult.deleted()).isEqualTo(expectedResult.deleted());
    }

    @Test
    public void createDeletedTest() {
        TgChat expectedResult = new TgChat(2L, 2L);

        repository.create(new TgChat(expectedResult.chatId()));
        TgChat actualResult = client.sql("SELECT * FROM tg_chats where id = ?")
                .param(expectedResult.id())
                .query(TgChat.class)
                .single();

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.chatId()).isEqualTo(expectedResult.chatId());
        assertThat(actualResult.deleted()).isEqualTo(expectedResult.deleted());
    }

    @Test
    public void createExistingTest() {
        TgChat expectedResult = new TgChat(1L, 1L);

        repository.create(new TgChat(expectedResult.chatId()));
        TgChat actualResult = client.sql("SELECT * FROM tg_chats where id = ?")
                .param(expectedResult.id())
                .query(TgChat.class)
                .single();

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.chatId()).isEqualTo(expectedResult.chatId());
        assertThat(actualResult.deleted()).isEqualTo(expectedResult.deleted());
    }

    @Test
    @SuppressWarnings("JavaTimeDefaultTimeZone")
    public void updateTest() {
        TgChat expectedResult = new TgChat(1L, 1L, false, List.of(), LocalTime.now());

        repository.update(expectedResult);

        TgChat actualResult = client.sql("select * from tg_chats where id = ?")
                .param(expectedResult.id())
                .query(TgChat.class)
                .single();

        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    public void deleteByIdTest() {
        long id = 1L;

        repository.deleteById(id);

        assertThat(client.sql("SELECT deleted FROM tg_chats WHERE id = ?")
                        .param(id)
                        .query(Boolean.class)
                        .single())
                .isTrue();
    }

    @Test
    public void deleteAlreadyDeletedByIdTest() {
        long id = 2L;

        repository.deleteById(id);

        assertThat(client.sql("SELECT deleted FROM tg_chats WHERE id = ?")
                        .param(id)
                        .query(Boolean.class)
                        .single())
                .isTrue();
    }

    @Test
    public void deleteTest() {
        TgChat chat = new TgChat(1L, 1L, true);

        repository.delete(chat);

        assertThat(client.sql("SELECT deleted FROM tg_chats WHERE id = ?")
                        .param(chat.id())
                        .query(Boolean.class)
                        .single())
                .isTrue();
    }

    @Test
    public void deleteAlreadyDeletedTest() {
        TgChat chat = new TgChat(2L, 2L, true);

        repository.delete(chat);

        assertThat(client.sql("SELECT deleted FROM tg_chats WHERE chat_id = ?")
                        .param(chat.chatId())
                        .query(Boolean.class)
                        .single())
                .isTrue();
    }
}
