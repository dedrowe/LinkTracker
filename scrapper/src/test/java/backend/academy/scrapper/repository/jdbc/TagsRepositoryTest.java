package backend.academy.scrapper.repository.jdbc;

import static backend.academy.scrapper.utils.FutureUnwrapper.unwrap;
import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.entity.LinkDataToTag;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.repository.tags.JdbcTagsRepository;
import backend.academy.shared.dto.TagLinkCount;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

public class TagsRepositoryTest extends AbstractJdbcTest {

    private final JdbcTagsRepository repository;

    @Autowired
    public TagsRepositoryTest(JdbcClient client) {
        super(client);
        repository = new JdbcTagsRepository(client);
    }

    @BeforeEach
    public void setUp() {
        client.sql("ALTER SEQUENCE links_data_id_seq RESTART WITH 1").update();
        client.sql("ALTER SEQUENCE links_id_seq RESTART WITH 1").update();
        client.sql("ALTER SEQUENCE tg_chats_id_seq RESTART WITH 1").update();
        client.sql("ALTER SEQUENCE links_data_to_tags_id_seq RESTART WITH 1").update();
        client.sql("ALTER SEQUENCE tags_id_seq RESTART WITH 1").update();

        client.sql("INSERT INTO tg_chats (chat_id) VALUES (1)").update();
        client.sql("INSERT INTO tg_chats (chat_id) VALUES (2)").update();
        client.sql("INSERT INTO links (link, last_update) VALUES ('https://example.com', '2025-03-13 17:23:25')")
                .update();
        client.sql("INSERT INTO links (link, last_update) VALUES ('https://example2.com', '2025-03-13 17:23:25')")
                .update();
        client.sql("INSERT INTO links_data (link_id, chat_id) VALUES (1, 1)").update();
        client.sql("INSERT INTO links_data (link_id, chat_id) VALUES (2, 2)").update();

        client.sql("INSERT INTO tags (tag) VALUES ('tag1')").update();
        client.sql("INSERT INTO tags (tag) VALUES ('tag2')").update();
        client.sql("INSERT INTO tags (tag) VALUES ('tag3')").update();

        client.sql("INSERT INTO links_data_to_tags (data_id, tag_id) VALUES (1, 1)")
                .update();
        client.sql("INSERT INTO links_data_to_tags (data_id, tag_id) VALUES (1, 2)")
                .update();
        client.sql("INSERT INTO links_data_to_tags (data_id, tag_id) VALUES (2, 3)")
                .update();
    }

    @Test
    public void getAllByDataIdTest() {
        Tag tag1 = new Tag(1L, "tag1");
        Tag tag2 = new Tag(2L, "tag2");

        List<Tag> actualResult = unwrap(repository.getAllByDataId(1));

        assertThat(actualResult).containsExactly(tag1, tag2);
    }

    @Test
    public void getAllByDataIdFailTest() {
        List<Tag> actualResult = unwrap(repository.getAllByDataId(-1));

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getTagLinksCountTest() {
        TagLinkCount tag1 = new TagLinkCount("tag1", 1);
        TagLinkCount tag2 = new TagLinkCount("tag2", 1);

        List<TagLinkCount> actualResult = unwrap(repository.getTagLinksCountByChatId(1));

        assertThat(actualResult).containsExactly(tag1, tag2);
    }

    @Test
    public void createAllTest() {
        Tag newTag = new Tag(4L, "tag4");
        LinkDataToTag data1 = new LinkDataToTag(1L, 1L, 1L);
        LinkDataToTag data3 = new LinkDataToTag(4L, 1L, 3L);
        LinkDataToTag data4 = new LinkDataToTag(5L, 1L, 4L);

        unwrap(repository.createAll(List.of("tag1", "tag3", newTag.tag()), 1L));
        List<LinkDataToTag> actualResult = client.sql("SELECT * FROM links_data_to_tags where data_id = 1")
                .query(LinkDataToTag.class)
                .list();
        Tag actualNewTag = client.sql("SELECT * FROM tags where tag = 'tag4'")
                .query(Tag.class)
                .single();

        assertThat(actualResult).containsExactly(data1, data3, data4);
        assertThat(actualNewTag).isEqualTo(newTag);
    }

    @Test
    public void deleteAllByDataIdTest() {

        unwrap(repository.deleteAllByDataId(1));

        assertThat(client.sql("SELECT * FROM links_data_to_tags where data_id = 1")
                        .query(Tag.class)
                        .list())
                .isEmpty();
        assertThat(client.sql("SELECT * FROM links_data_to_tags where data_id = 2")
                        .query(Tag.class)
                        .list()
                        .size())
                .isEqualTo(1);
    }
}
