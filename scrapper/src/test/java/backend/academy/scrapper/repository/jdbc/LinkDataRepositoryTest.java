package backend.academy.scrapper.repository.jdbc;

import static backend.academy.scrapper.utils.FutureUnwrapper.unwrap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.exceptionHandling.exceptions.LinkDataException;
import backend.academy.scrapper.repository.linkdata.JdbcLinkDataRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

public class LinkDataRepositoryTest extends AbstractJdbcTest {

    private final JdbcLinkDataRepository repository;

    @Autowired
    public LinkDataRepositoryTest(JdbcClient client) {
        super(client);
        repository = new JdbcLinkDataRepository(client);
    }

    @BeforeEach
    void setUp() {
        client.sql("ALTER SEQUENCE links_data_id_seq RESTART WITH 1").update();
        client.sql("ALTER SEQUENCE links_id_seq RESTART WITH 1").update();
        client.sql("ALTER SEQUENCE tg_chats_id_seq RESTART WITH 1").update();

        client.sql("INSERT INTO tg_chats (chat_id, deleted) VALUES (1, false)").update();
        client.sql(
                        "INSERT INTO links (link, last_update, deleted) VALUES ('https://example.com', '2025-03-13 17:23:25', false)")
                .update();
        client.sql("INSERT INTO tg_chats (chat_id, deleted) VALUES (2, false)").update();
        client.sql(
                        "INSERT INTO links (link, last_update, deleted) VALUES ('https://example2.com', '2025-03-13 17:23:25', false)")
                .update();
        client.sql("INSERT INTO tg_chats (chat_id, deleted) VALUES (3, false)").update();
        client.sql(
                        "INSERT INTO links (link, last_update, deleted) VALUES ('https://example3.com', '2025-03-13 17:23:25', false)")
                .update();

        client.sql("INSERT INTO links_data (link_id, chat_id, deleted) VALUES (1, 1, false)")
                .update();
        client.sql("INSERT INTO links_data (link_id, chat_id, deleted) VALUES (2, 2, true)")
                .update();
    }

    @Test
    public void getAllTest() {
        LinkData expectedResult = new LinkData(1L, 1L, 1L);

        List<LinkData> actualResult = unwrap(repository.getAll());

        assertThat(actualResult.size()).isEqualTo(1);
        assertThat(actualResult.getFirst().id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.getFirst().linkId()).isEqualTo(expectedResult.linkId());
        assertThat(actualResult.getFirst().chatId()).isEqualTo(expectedResult.chatId());
    }

    @Test
    public void getByIdTest() {
        long id = 1L;
        LinkData expectedResult = new LinkData(id, 1L, 1L);

        LinkData actualResult = unwrap(repository.getById(id)).get();

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.linkId()).isEqualTo(expectedResult.linkId());
        assertThat(actualResult.chatId()).isEqualTo(expectedResult.chatId());
    }

    @Test
    public void getDeletedByIdTest() {
        Optional<LinkData> actualResult = unwrap(repository.getById(2));

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByIdFailTest() {
        Optional<LinkData> actualResult = unwrap(repository.getById(-1));

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByChatIdTest() {
        long id = 1L;
        LinkData expectedResult = new LinkData(1L, 1L, id);

        List<LinkData> actualResult = unwrap(repository.getByChatId(id));

        assertThat(actualResult.size()).isEqualTo(1);
        assertThat(actualResult.getFirst().id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.getFirst().linkId()).isEqualTo(expectedResult.linkId());
        assertThat(actualResult.getFirst().chatId()).isEqualTo(expectedResult.chatId());
    }

    @Test
    public void getDeletedByChatIdTest() {
        List<LinkData> actualResult = unwrap(repository.getByChatId(2));

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByChatIdFailTest() {
        List<LinkData> actualResult = unwrap(repository.getByChatId(-1));

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByLinkIdTest() {
        long id = 1L;
        LinkData expectedResult = new LinkData(1L, id, 1L);

        List<LinkData> actualResult = unwrap(repository.getByLinkId(id));

        assertThat(actualResult.size()).isEqualTo(1);
        assertThat(actualResult.getFirst().id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.getFirst().linkId()).isEqualTo(expectedResult.linkId());
        assertThat(actualResult.getFirst().chatId()).isEqualTo(expectedResult.chatId());
    }

    @Test
    public void getByLinkIdWithSkipLimitTest() {
        long skip = 1L;
        long limit = 2L;
        client.sql("INSERT INTO tg_chats (chat_id, deleted) VALUES (4, false)").update();
        client.sql("INSERT INTO links_data (link_id, chat_id, deleted) VALUES (2, 1, false)")
                .update();
        client.sql("INSERT INTO links_data (link_id, chat_id, deleted) VALUES (2, 3, false)")
                .update();
        client.sql("INSERT INTO links_data (link_id, chat_id, deleted) VALUES (2, 4, false)")
                .update();
        LinkData linkData1 = new LinkData(4L, 2L, 3L);
        LinkData linkData2 = new LinkData(5L, 2L, 4L);

        List<LinkData> actualResult = unwrap(repository.getByLinkId(2L, skip, limit));

        assertThat(actualResult).containsExactly(linkData1, linkData2);
    }

    @Test
    public void getDeletedByLinkIdTest() {
        List<LinkData> actualResult = unwrap(repository.getByLinkId(2));

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByLinkIdFailTest() {
        List<LinkData> actualResult = unwrap(repository.getByLinkId(-1));

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByChatIdLinkIdTest() {
        long linkId = 1L;
        long chatId = 1L;
        LinkData expectedResult = new LinkData(1L, linkId, chatId);

        LinkData actualResult =
                unwrap(repository.getByChatIdLinkId(chatId, linkId)).get();

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.linkId()).isEqualTo(expectedResult.linkId());
        assertThat(actualResult.chatId()).isEqualTo(expectedResult.chatId());
    }

    @Test
    public void getDeletedByChatIdLinkIdTest() {
        Optional<LinkData> actualResult = unwrap(repository.getByChatIdLinkId(2L, 2L));

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByChatIdLinkIdFailTest() {
        Optional<LinkData> actualResult = unwrap(repository.getByChatIdLinkId(-1L, -1L));

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByTagAndChatIdTest() {
        client.sql("INSERT INTO links_data (link_id, chat_id, deleted) VALUES (2, 1, false)")
                .update();
        client.sql("INSERT INTO links_data (link_id, chat_id, deleted) VALUES (3, 1, false)")
                .update();
        client.sql("INSERT INTO tags (tag) values ('test')").update();
        client.sql("INSERT INTO links_data_to_tags (data_id, tag_id) VALUES (1, 1)")
                .update();
        client.sql("INSERT INTO links_data_to_tags (data_id, tag_id) VALUES (3, 1)")
                .update();
        LinkData linkData1 = new LinkData(1L, 1L, 1L);
        LinkData linkData2 = new LinkData(3L, 2L, 1L);

        List<LinkData> actualResult = unwrap(repository.getByTagAndChatId("test", 1));

        assertThat(actualResult).containsExactly(linkData1, linkData2);
    }

    @Test
    public void createNewTest() {
        long chatId = 3L;
        long linkId = 3L;
        LinkData expectedResult = new LinkData(3L, linkId, chatId);

        unwrap(repository.create(expectedResult));
        LinkData actualResult = client.sql("SELECT * FROM links_data WHERE chat_id = :chatId and link_id = :linkId")
                .param("chatId", chatId)
                .param("linkId", linkId)
                .query(LinkData.class)
                .single();

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.linkId()).isEqualTo(expectedResult.linkId());
        assertThat(actualResult.chatId()).isEqualTo(expectedResult.chatId());
    }

    @Test
    public void createDeletedTest() {
        long chatId = 2L;
        long linkId = 2L;
        LinkData expectedResult = new LinkData(2L, linkId, chatId);

        unwrap(repository.create(expectedResult));
        LinkData actualResult = client.sql("SELECT * FROM links_data WHERE chat_id = :chatId and link_id = :linkId")
                .param("chatId", chatId)
                .param("linkId", linkId)
                .query(LinkData.class)
                .single();

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.linkId()).isEqualTo(expectedResult.linkId());
        assertThat(actualResult.chatId()).isEqualTo(expectedResult.chatId());
    }

    @Test
    public void createExistingTest() {
        LinkData expectedResult = new LinkData(1L, 1L, 1L);

        assertThatThrownBy(() -> unwrap(repository.create(expectedResult))).isInstanceOf(LinkDataException.class);
    }

    @Test
    public void deleteByIdTest() {
        long id = 1L;

        unwrap(repository.deleteById(id));

        assertThat(client.sql("SELECT deleted FROM links_data WHERE id = ?")
                        .param(id)
                        .query(Boolean.class)
                        .single())
                .isTrue();
    }

    @Test
    public void deleteAlreadyDeletedByIdTest() {
        long id = 2L;

        unwrap(repository.deleteById(id));

        assertThat(client.sql("SELECT deleted FROM links_data WHERE id = ?")
                        .param(id)
                        .query(Boolean.class)
                        .single())
                .isTrue();
    }

    @Test
    public void deleteTest() {
        LinkData linkData = new LinkData(1L, 1L, 1L);

        unwrap(repository.deleteLinkData(linkData));

        assertThat(client.sql("SELECT deleted FROM links_data WHERE id = ?")
                        .param(linkData.id())
                        .query(Boolean.class)
                        .single())
                .isTrue();
    }

    @Test
    public void deleteAlreadyDeletedTest() {
        LinkData linkData = new LinkData(2L, 2L, 2L);

        unwrap(repository.deleteLinkData(linkData));

        assertThat(client.sql("SELECT deleted FROM links_data WHERE id = ?")
                        .param(linkData.id())
                        .query(Boolean.class)
                        .single())
                .isTrue();
    }
}
