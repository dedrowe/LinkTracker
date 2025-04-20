package backend.academy.scrapper.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkDataToTag;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.entity.TgChat;
import backend.academy.scrapper.entity.jpa.JpaLinkData;
import backend.academy.scrapper.repository.linkdata.JpaLinkDataRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import backend.academy.scrapper.utils.UtcDateTimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

public class LinkDataRepositoryTest extends AbstractJpaTest {

    private final JpaLinkDataRepository repository;

    private final LocalDateTime testTimestamp = UtcDateTimeProvider.of(1741886605);

    @Autowired
    public LinkDataRepositoryTest(TestEntityManager entityManager, JpaLinkDataRepository repository) {
        super(entityManager);
        this.repository = repository;
    }

    private TgChat tgChat1 = new TgChat(1);

    private TgChat tgChat2 = new TgChat(2);

    private TgChat tgChat3 = new TgChat(3);

    private Link link1 = new Link(null, "https://example.com", testTimestamp);

    private Link link2 = new Link(null, "https://example2.com", testTimestamp);

    private Link link3 = new Link(null, "https://example3.com", testTimestamp);

    @BeforeEach
    public void setUp() {
        entityManager
                .getEntityManager()
                .createNativeQuery("ALTER SEQUENCE links_data_id_seq RESTART WITH 1")
                .executeUpdate();
        entityManager
                .getEntityManager()
                .createNativeQuery("ALTER SEQUENCE links_id_seq RESTART WITH 1")
                .executeUpdate();
        entityManager
                .getEntityManager()
                .createNativeQuery("ALTER SEQUENCE tg_chats_id_seq RESTART WITH 1")
                .executeUpdate();

        entityManager.persist(tgChat1);
        entityManager.persist(tgChat2);
        entityManager.persist(tgChat3);
        entityManager.persist(link1);
        entityManager.persist(link2);
        entityManager.persist(link3);
        entityManager.persist(new JpaLinkData(link1, tgChat1));
        entityManager.persist(new JpaLinkData(link2, tgChat2, true));
        entityManager.flush();
    }

    @Test
    public void getAllTest() {
        JpaLinkData expectedResult = new JpaLinkData(1L, link1, tgChat1);

        List<JpaLinkData> actualResult = repository.getAll();

        assertThat(actualResult.size()).isEqualTo(1);
        assertThat(actualResult.getFirst().id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.getFirst().link().id()).isEqualTo(expectedResult.linkId());
        assertThat(actualResult.getFirst().tgChat().id()).isEqualTo(expectedResult.chatId());
    }

    @Test
    public void getByIdTest() {
        long id = 1L;
        JpaLinkData expectedResult = new JpaLinkData(id, link1, tgChat1);

        JpaLinkData actualResult = repository.getById(id).get();

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.link().id()).isEqualTo(expectedResult.linkId());
        assertThat(actualResult.tgChat().id()).isEqualTo(expectedResult.chatId());
    }

    @Test
    public void getDeletedByIdTest() {
        Optional<JpaLinkData> actualResult = repository.getById(2);

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByIdFailTest() {
        Optional<JpaLinkData> actualResult = repository.getById(-1);

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByChatIdTest() {
        long id = tgChat1.id();
        JpaLinkData expectedResult = new JpaLinkData(1L, link1, tgChat1);

        List<JpaLinkData> actualResult = repository.getByChatId(id);

        assertThat(actualResult.size()).isEqualTo(1);
        assertThat(actualResult.getFirst().id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.getFirst().link().id()).isEqualTo(expectedResult.linkId());
        assertThat(actualResult.getFirst().tgChat().id()).isEqualTo(expectedResult.chatId());
    }

    @Test
    public void getDeletedByChatIdTest() {
        List<JpaLinkData> actualResult = repository.getByChatId(2);

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByChatIdFailTest() {
        List<JpaLinkData> actualResult = repository.getByChatId(-1);

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByLinkIdTest() {
        long id = link1.id();
        JpaLinkData expectedResult = new JpaLinkData(1L, link1, tgChat1);

        List<JpaLinkData> actualResult = repository.getByLinkId(id);

        assertThat(actualResult.size()).isEqualTo(1);
        assertThat(actualResult.getFirst().id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.getFirst().link().id()).isEqualTo(expectedResult.linkId());
        assertThat(actualResult.getFirst().tgChat().id()).isEqualTo(expectedResult.chatId());
    }

    @Test
    public void getDeletedByLinkIdTest() {
        List<JpaLinkData> actualResult = repository.getByLinkId(2);

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByLinkIdFailTest() {
        List<JpaLinkData> actualResult = repository.getByLinkId(-1);

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByLinkIdWithSkipLimitTest() {
        long skip = 3L;
        long limit = 2L;
        TgChat tgChat4 = new TgChat(4);

        JpaLinkData linkData1 = new JpaLinkData(link2, tgChat1);
        JpaLinkData linkData2 = new JpaLinkData(link2, tgChat3);
        JpaLinkData linkData3 = new JpaLinkData(link2, tgChat4);

        entityManager.persist(tgChat4);
        entityManager.persist(linkData1);
        entityManager.persist(linkData2);
        entityManager.persist(linkData3);
        entityManager.flush();

        List<JpaLinkData> actualResult = repository.getByLinkId(2L, skip, limit);

        assertThat(actualResult).containsExactly(linkData2, linkData3);
    }

    @Test
    public void getByChatIdLinkIdTest() {
        long linkId = link1.id();
        long chatId = tgChat1.id();
        JpaLinkData expectedResult = new JpaLinkData(1L, link1, tgChat1);

        JpaLinkData actualResult = repository.getByChatIdLinkId(chatId, linkId).get();

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.link().id()).isEqualTo(expectedResult.linkId());
        assertThat(actualResult.tgChat().id()).isEqualTo(expectedResult.chatId());
    }

    @Test
    public void getDeletedByChatIdLinkIdTest() {
        Optional<JpaLinkData> actualResult = repository.getByChatIdLinkId(2, 2);

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByChatIdLinkIdFailTest() {
        Optional<JpaLinkData> actualResult = repository.getByChatIdLinkId(-1, -1);

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByTagAndChatIdTest() {

        JpaLinkData linkData1 = new JpaLinkData(null, link2.id(), tgChat1.id(), false, link2, tgChat1, null, null);
        JpaLinkData linkData2 = new JpaLinkData(null, link3.id(), tgChat1.id(), false, link3, tgChat1, null, null);
        Tag tag = new Tag(null, "test");

        entityManager.persist(linkData1);
        entityManager.persist(linkData2);
        entityManager.persist(tag);
        entityManager.flush();

        LinkDataToTag linkDataToTag1 = new LinkDataToTag(null, linkData1.id(), tag.id());
        LinkDataToTag linkDataToTag2 = new LinkDataToTag(null, linkData2.id(), tag.id());

        entityManager.persist(linkDataToTag1);
        entityManager.persist(linkDataToTag2);
        entityManager.flush();

        List<JpaLinkData> actualResult = repository.getByTagAndChatId("test", 1);

        assertThat(actualResult).contains(linkData1, linkData2);
        assertThat(actualResult.size()).isEqualTo(2);
    }

    @Test
    public void createNewTest() {
        long expectedId = 3L;
        JpaLinkData expectedResult = new JpaLinkData(null, link3.id(), tgChat3.id(), false, link3, tgChat3, null, null);
        repository.create(expectedResult);
        JpaLinkData actualResult = entityManager.find(JpaLinkData.class, expectedResult.id());

        assertThat(actualResult.id()).isEqualTo(expectedId);
        assertThat(actualResult.linkId()).isEqualTo(expectedResult.linkId());
        assertThat(actualResult.chatId()).isEqualTo(expectedResult.chatId());
    }

    @Test
    public void createDeletedTest() {
        long chatId = tgChat2.id();
        long linkId = link2.id();
        JpaLinkData expectedResult = new JpaLinkData(2L, link2, tgChat2);

        repository.create(expectedResult);
        JpaLinkData actualResult = entityManager.find(JpaLinkData.class, expectedResult.id());

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.linkId()).isEqualTo(expectedResult.linkId());
        assertThat(actualResult.chatId()).isEqualTo(expectedResult.chatId());
    }

    @Test
    public void createExistingTest() {
        long expectedId = 1L;
        JpaLinkData expectedResult = new JpaLinkData(null, link1.id(), tgChat1.id(), false, link1, tgChat1, null, null);

        repository.create(expectedResult);

        assertThat(expectedResult.id()).isEqualTo(expectedId);
    }

    @Test
    public void deleteByIdTest() {
        long id = 1L;

        repository.deleteById(id);
        entityManager.clear();
        JpaLinkData actualResult = (JpaLinkData) entityManager
                .getEntityManager()
                .createNativeQuery("select * from links_data where id = :id", JpaLinkData.class)
                .setParameter("id", id)
                .getSingleResult();

        assertThat(actualResult.deleted()).isTrue();
    }

    @Test
    public void deleteAlreadyDeletedByIdTest() {
        long id = 2L;

        repository.deleteById(id);
        JpaLinkData actualResult = (JpaLinkData) entityManager
                .getEntityManager()
                .createNativeQuery("select * from links_data where id = :id", JpaLinkData.class)
                .setParameter("id", id)
                .getSingleResult();

        assertThat(actualResult.deleted()).isTrue();
    }

    @Test
    public void deleteTest() {
        JpaLinkData linkData = new JpaLinkData(1L, link1, tgChat1);

        repository.deleteInternal(linkData.chatId(), linkData.linkId());
        entityManager.clear();
        JpaLinkData actualResult = (JpaLinkData) entityManager
                .getEntityManager()
                .createNativeQuery("select * from links_data where id = :id", JpaLinkData.class)
                .setParameter("id", linkData.id())
                .getSingleResult();

        assertThat(actualResult.deleted()).isTrue();
    }

    @Test
    public void deleteAlreadyDeletedTest() {
        JpaLinkData linkData = new JpaLinkData(2L, link2, tgChat2);

        repository.deleteInternal(linkData.chatId(), linkData.linkId());
        entityManager.clear();
        JpaLinkData actualResult = (JpaLinkData) entityManager
                .getEntityManager()
                .createNativeQuery("select * from links_data where id = :id", JpaLinkData.class)
                .setParameter("id", linkData.id())
                .getSingleResult();

        assertThat(actualResult.deleted()).isTrue();
    }
}
