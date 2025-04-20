package backend.academy.scrapper.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkDataTagDto;
import backend.academy.scrapper.entity.LinkDataToTag;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.entity.TgChat;
import backend.academy.scrapper.entity.jpa.JpaLinkData;
import backend.academy.scrapper.repository.tags.JpaTagsRepository;
import backend.academy.scrapper.utils.UtcDateTimeProvider;
import backend.academy.shared.dto.TagLinkCount;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

public class TagsRepositoryTest extends AbstractJpaTest {

    private final JpaTagsRepository repository;

    private final LocalDateTime testTimestamp = UtcDateTimeProvider.of(1741886605);

    @Autowired
    public TagsRepositoryTest(TestEntityManager entityManager, JpaTagsRepository repository) {
        super(entityManager);
        this.repository = repository;
    }

    private TgChat tgChat1 = new TgChat(1);
    private TgChat tgChat2 = new TgChat(2);

    private Link link1 = new Link(null, "https://example.com", testTimestamp);
    private Link link2 = new Link(null, "https://example2.com", testTimestamp);

    private JpaLinkData linkData1 = new JpaLinkData(link1, tgChat1);
    private JpaLinkData linkData2 = new JpaLinkData(link2, tgChat2);

    private Tag tag1 = new Tag("tag1");
    private Tag tag2 = new Tag("tag2");
    private Tag tag3 = new Tag("tag3");

    private LinkDataToTag linkDataToTag1;
    private LinkDataToTag linkDataToTag2;
    private LinkDataToTag linkDataToTag3;

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
        entityManager
                .getEntityManager()
                .createNativeQuery("ALTER SEQUENCE links_data_to_tags_id_seq RESTART WITH 1")
                .executeUpdate();
        entityManager
                .getEntityManager()
                .createNativeQuery("ALTER SEQUENCE tags_id_seq RESTART WITH 1")
                .executeUpdate();
        entityManager.persist(tgChat1);
        entityManager.persist(tgChat2);
        entityManager.persist(link1);
        entityManager.persist(link2);
        entityManager.persist(linkData1);
        entityManager.persist(linkData2);
        entityManager.persist(tag1);
        entityManager.persist(tag2);
        entityManager.persist(tag3);
        entityManager.flush();

        linkDataToTag1 = new LinkDataToTag(linkData1.id(), tag1.id());
        linkDataToTag2 = new LinkDataToTag(linkData1.id(), tag2.id());
        linkDataToTag3 = new LinkDataToTag(linkData2.id(), tag3.id());
        entityManager.persist(linkDataToTag1);
        entityManager.persist(linkDataToTag2);
        entityManager.persist(linkDataToTag3);
        entityManager.flush();
    }

    @Test
    public void getAllByDataIdTest() {
        List<Tag> actualResult = repository.getAllByDataId(linkData1.id());

        assertThat(actualResult).containsExactly(tag1, tag2);
    }

    @Test
    public void getAllByDataIds() {
        LinkDataTagDto tag1 = new LinkDataTagDto("tag1", 1L);
        LinkDataTagDto tag2 = new LinkDataTagDto("tag2", 1L);
        LinkDataTagDto tag3 = new LinkDataTagDto("tag3", 2L);

        List<LinkDataTagDto> actualResult = repository.getAllByDataIds(List.of(1L, 2L));

        assertThat(actualResult).containsExactly(tag1, tag2, tag3);
    }

    @Test
    public void getAllByDataIdFailTest() {
        List<Tag> actualResult = repository.getAllByDataId(-1);

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getTagLinksCountTest() {
        TagLinkCount tag1 = new TagLinkCount("tag1", 1);
        TagLinkCount tag2 = new TagLinkCount("tag2", 1);

        List<TagLinkCount> actualResult = repository.getTagLinksCountByChatId(linkData1.id());

        assertThat(actualResult).containsExactly(tag1, tag2);
    }

    @Test
    public void getAllByTagsSetTest() {
        Set<String> tags = Set.of(tag1.tag(), tag2.tag(), "tag4");

        List<Tag> actualResult = repository.getAllByTagsSet(tags);

        assertThat(actualResult).containsExactly(tag1, tag2);
    }

    @Test
    public void createTagTest() {
        long newId = 4L;
        Tag tag = new Tag("tag4");

        repository.createTag(tag);

        assertThat(tag.id()).isEqualTo(newId);
    }

    @Test
    public void createRelationTest() {
        assertThat(entityManager
                        .getEntityManager()
                        .createQuery("select l from LinkDataToTag l where l.dataId = 2 and l.tagId = 1")
                        .getResultList())
                .isEmpty();

        repository.createRelation(2, 1);

        assertThat(entityManager
                        .getEntityManager()
                        .createQuery("select l from LinkDataToTag l where l.dataId = 2 and l.tagId = 1")
                        .getResultList()
                        .size())
                .isEqualTo(1);
    }

    @Test
    public void deleteRelationTest() {
        repository.deleteRelation(2, 3);

        assertThat(entityManager
                        .getEntityManager()
                        .createQuery("select l from LinkDataToTag l where l.dataId = 2 and l.tagId = 3")
                        .getResultList())
                .isEmpty();
    }

    @Test
    public void deleteAllByDataIdTest() {
        repository.deleteAllByDataId(linkData1.id());

        assertThat(entityManager
                        .getEntityManager()
                        .createQuery("select l from LinkDataToTag l where l.dataId = 1")
                        .getResultList())
                .isEmpty();
        assertThat(entityManager
                        .getEntityManager()
                        .createQuery("select l from LinkDataToTag l where l.dataId = 2")
                        .getResultList()
                        .size())
                .isEqualTo(1);
    }
}
