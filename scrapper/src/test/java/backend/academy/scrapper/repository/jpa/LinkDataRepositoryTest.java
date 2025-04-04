package backend.academy.scrapper.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.entity.LinkDataToTag;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.entity.TgChat;
import backend.academy.scrapper.repository.linkdata.JpaLinkDataRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

public class LinkDataRepositoryTest extends AbstractJpaTest {

    private final JpaLinkDataRepository repository;

    private final LocalDateTime testTimestamp =
            Instant.ofEpochSecond(1741886605).atZone(ZoneOffset.UTC).toLocalDateTime();

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
        entityManager.persist(new LinkData(link1, tgChat1));
        entityManager.persist(new LinkData(link2, tgChat2, true));
        entityManager.flush();
    }

    @Test
    public void getAllTest() {
        LinkData expectedResult = new LinkData(1L, 1L, 1L);

        List<LinkData> actualResult = repository.getAllSync();

        assertThat(actualResult.size()).isEqualTo(1);
        assertThat(actualResult.getFirst().id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.getFirst().link().id()).isEqualTo(expectedResult.getLinkId());
        assertThat(actualResult.getFirst().tgChat().id()).isEqualTo(expectedResult.chatId());
    }

    @Test
    public void getByIdTest() {
        long id = 1L;
        LinkData expectedResult = new LinkData(id, 1L, 1L);

        LinkData actualResult = repository.getByIdSync(id).get();

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.link().id()).isEqualTo(expectedResult.getLinkId());
        assertThat(actualResult.tgChat().id()).isEqualTo(expectedResult.chatId());
    }

    @Test
    public void getDeletedByIdTest() {
        Optional<LinkData> actualResult = repository.getByIdSync(2);

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByIdFailTest() {
        Optional<LinkData> actualResult = repository.getByIdSync(-1);

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByChatIdTest() {
        long id = 1L;
        LinkData expectedResult = new LinkData(1L, 1L, id);

        List<LinkData> actualResult = repository.getByChatIdSync(id);

        assertThat(actualResult.size()).isEqualTo(1);
        assertThat(actualResult.getFirst().id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.getFirst().link().id()).isEqualTo(expectedResult.getLinkId());
        assertThat(actualResult.getFirst().tgChat().id()).isEqualTo(expectedResult.chatId());
    }

    @Test
    public void getDeletedByChatIdTest() {
        List<LinkData> actualResult = repository.getByChatIdSync(2);

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByChatIdFailTest() {
        List<LinkData> actualResult = repository.getByChatIdSync(-1);

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByLinkIdTest() {
        long id = 1L;
        LinkData expectedResult = new LinkData(1L, id, 1L);

        List<LinkData> actualResult = repository.getByLinkIdSync(id);

        assertThat(actualResult.size()).isEqualTo(1);
        assertThat(actualResult.getFirst().id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.getFirst().link().id()).isEqualTo(expectedResult.getLinkId());
        assertThat(actualResult.getFirst().tgChat().id()).isEqualTo(expectedResult.chatId());
    }

    @Test
    public void getDeletedByLinkIdTest() {
        List<LinkData> actualResult = repository.getByLinkIdSync(2);

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByLinkIdFailTest() {
        List<LinkData> actualResult = repository.getByLinkIdSync(-1);

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByLinkIdWithSkipLimitTest() {
        long skip = 3L;
        long limit = 2L;
        TgChat tgChat4 = new TgChat(4);

        LinkData linkData1 = new LinkData(link2, tgChat1);
        LinkData linkData2 = new LinkData(link2, tgChat3);
        LinkData linkData3 = new LinkData(link2, tgChat4);

        entityManager.persist(tgChat4);
        entityManager.persist(linkData1);
        entityManager.persist(linkData2);
        entityManager.persist(linkData3);
        entityManager.flush();

        List<LinkData> actualResult = repository.getByLinkIdSync(2L, skip, limit);

        assertThat(actualResult).containsExactly(linkData2, linkData3);
    }

    @Test
    public void getByChatIdLinkIdTest() {
        long linkId = 1L;
        long chatId = 1L;
        LinkData expectedResult = new LinkData(1L, linkId, chatId);

        LinkData actualResult = repository.getByChatIdLinkIdSync(chatId, linkId).get();

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.link().id()).isEqualTo(expectedResult.getLinkId());
        assertThat(actualResult.tgChat().id()).isEqualTo(expectedResult.chatId());
    }

    @Test
    public void getDeletedByChatIdLinkIdTest() {
        Optional<LinkData> actualResult = repository.getByChatIdLinkIdSync(2, 2);

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByChatIdLinkIdFailTest() {
        Optional<LinkData> actualResult = repository.getByChatIdLinkIdSync(-1, -1);

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByTagAndChatIdTest() {

        LinkData linkData1 = new LinkData(null, link2.id(), tgChat1.id(), false, link2, tgChat1, null, null);
        LinkData linkData2 = new LinkData(null, link3.id(), tgChat1.id(), false, link3, tgChat1, null, null);
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

        List<LinkData> actualResult = repository.getByTagAndChatIdSync("test", 1);

        assertThat(actualResult).contains(linkData1, linkData2);
        assertThat(actualResult.size()).isEqualTo(2);
    }

    @Test
    public void createNewTest() {
        long expectedId = 3L;
        LinkData expectedResult = new LinkData(null, link3.id(), tgChat3.id(), false, link3, tgChat3, null, null);
        repository.createSync(expectedResult);
        LinkData actualResult = entityManager.find(LinkData.class, expectedResult.id());

        assertThat(actualResult.id()).isEqualTo(expectedId);
        assertThat(actualResult.getLinkId()).isEqualTo(expectedResult.getLinkId());
        assertThat(actualResult.chatId()).isEqualTo(expectedResult.chatId());
    }

    @Test
    public void createDeletedTest() {
        long chatId = 2L;
        long linkId = 2L;
        LinkData expectedResult = new LinkData(2L, linkId, chatId);

        repository.createSync(expectedResult);
        LinkData actualResult = entityManager.find(LinkData.class, expectedResult.id());

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.getLinkId()).isEqualTo(expectedResult.getLinkId());
        assertThat(actualResult.chatId()).isEqualTo(expectedResult.chatId());
    }

    @Test
    public void createExistingTest() {
        long expectedId = 1L;
        LinkData expectedResult = new LinkData(null, link1.id(), tgChat1.id(), false, link1, tgChat1, null, null);

        repository.createSync(expectedResult);

        assertThat(expectedResult.id()).isEqualTo(expectedId);
    }

    @Test
    public void deleteByIdTest() {
        long id = 1L;

        repository.deleteByIdSync(id);
        entityManager.clear();
        LinkData actualResult = (LinkData) entityManager
                .getEntityManager()
                .createNativeQuery("select * from links_data where id = :id", LinkData.class)
                .setParameter("id", id)
                .getSingleResult();

        assertThat(actualResult.deleted()).isTrue();
    }

    @Test
    public void deleteAlreadyDeletedByIdTest() {
        long id = 2L;

        repository.deleteByIdSync(id);
        LinkData actualResult = (LinkData) entityManager
                .getEntityManager()
                .createNativeQuery("select * from links_data where id = :id", LinkData.class)
                .setParameter("id", id)
                .getSingleResult();

        assertThat(actualResult.deleted()).isTrue();
    }

    @Test
    public void deleteTest() {
        LinkData linkData = new LinkData(1L, 1L, 1L);

        repository.deleteSync(linkData.chatId(), linkData.getLinkId());
        entityManager.clear();
        LinkData actualResult = (LinkData) entityManager
                .getEntityManager()
                .createNativeQuery("select * from links_data where id = :id", LinkData.class)
                .setParameter("id", linkData.id())
                .getSingleResult();

        assertThat(actualResult.deleted()).isTrue();
    }

    @Test
    public void deleteAlreadyDeletedTest() {
        LinkData linkData = new LinkData(2L, 2L, 2L);

        repository.deleteSync(linkData.chatId(), linkData.getLinkId());
        entityManager.clear();
        LinkData actualResult = (LinkData) entityManager
                .getEntityManager()
                .createNativeQuery("select * from links_data where id = :id", LinkData.class)
                .setParameter("id", linkData.id())
                .getSingleResult();

        assertThat(actualResult.deleted()).isTrue();
    }
}
