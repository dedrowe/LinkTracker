package backend.academy.scrapper.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import backend.academy.scrapper.entity.TgChat;
import backend.academy.scrapper.exceptionHandling.exceptions.TgChatException;
import backend.academy.scrapper.repository.tgchat.JpaTgChatRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

public class TgChatRepositoryTest extends AbstractJpaTest {

    private final JpaTgChatRepository repository;

    @Autowired
    public TgChatRepositoryTest(TestEntityManager entityManager, JpaTgChatRepository repository) {
        super(entityManager);
        this.repository = repository;
    }

    @BeforeEach
    public void setUp() {
        entityManager
                .getEntityManager()
                .createNativeQuery("ALTER SEQUENCE tg_chats_id_seq RESTART WITH 1")
                .executeUpdate();
        entityManager.persist(new TgChat(1));
        entityManager.persist(new TgChat(null, 2, true));
    }

    @Test
    public void getByIdTest() {
        TgChat expectedResult = new TgChat(1L, 1L);

        TgChat actualResult = repository.getByIdSync(expectedResult.id()).get();

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.chatId()).isEqualTo(expectedResult.chatId());
        assertThat(actualResult.deleted()).isEqualTo(expectedResult.deleted());
    }

    @Test
    public void getDeletedByIdTest() {
        Optional<TgChat> actualResult = repository.getByIdSync(2);

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByIdFailTest() {
        Optional<TgChat> actualResult = repository.getByIdSync(-1);

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByChatIdTest() {
        TgChat expectedResult = new TgChat(1L, 1L);

        TgChat actualResult =
                repository.getByChatIdSync(expectedResult.chatId()).get();

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.chatId()).isEqualTo(expectedResult.chatId());
        assertThat(actualResult.deleted()).isEqualTo(expectedResult.deleted());
    }

    @Test
    public void getDeletedByChatIdTest() {
        Optional<TgChat> actualResult = repository.getByChatIdSync(2);

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByChatIdFailTest() {
        Optional<TgChat> actualResult = repository.getByChatIdSync(-1);

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void createNewTest() {
        TgChat expectedResult = new TgChat(null, 3L);

        repository.createSync(expectedResult);
        TgChat actualResult = entityManager.find(TgChat.class, expectedResult.id());

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.chatId()).isEqualTo(expectedResult.chatId());
        assertThat(actualResult.deleted()).isEqualTo(expectedResult.deleted());
    }

    @Test
    public void createDeletedTest() {
        long expectedId = 2L;
        TgChat expectedResult = new TgChat(null, 2L);

        repository.createSync(expectedResult);
        entityManager.clear();
        TgChat actualResult = entityManager.find(TgChat.class, expectedId);

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.chatId()).isEqualTo(expectedResult.chatId());
        assertThat(actualResult.deleted()).isEqualTo(expectedResult.deleted());
    }

    @Test
    public void createExistingTest() {
        TgChat expectedResult = new TgChat(1L, 1L);

        assertThatThrownBy(() -> repository.createSync(new TgChat(expectedResult.chatId())))
                .isInstanceOf(TgChatException.class);
    }

    @Test
    public void deleteByIdTest() {
        long id = 1L;

        repository.deleteByIdSync(id);
        entityManager.clear();
        TgChat actualResult = entityManager.find(TgChat.class, id);

        assertThat(actualResult.deleted()).isTrue();
    }

    @Test
    public void deleteAlreadyByIdTest() {
        long id = 2L;

        repository.deleteByIdSync(id);
        TgChat actualResult = entityManager.find(TgChat.class, id);

        assertThat(actualResult.deleted()).isTrue();
    }

    @Test
    public void deleteTest() {
        TgChat chat = new TgChat(1L, 1L, true);

        repository.deleteSync(chat.chatId());
        entityManager.clear();
        TgChat actualResult = entityManager.find(TgChat.class, chat.id());

        assertThat(actualResult.deleted()).isTrue();
    }

    @Test
    public void deleteAlreadyTest() {
        TgChat chat = new TgChat(2L, 2L, true);

        repository.deleteSync(chat.chatId());
        entityManager.clear();
        TgChat actualResult = entityManager.find(TgChat.class, chat.id());

        assertThat(actualResult.deleted()).isTrue();
    }
}
