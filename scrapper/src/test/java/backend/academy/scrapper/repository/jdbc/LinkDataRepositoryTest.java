package backend.academy.scrapper.repository.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.entity.jdbc.JdbcLinkData;
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
        client.sql("ALTER SEQUENCE tags_id_seq RESTART WITH 1").update();

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
        JdbcLinkData expectedResult = new JdbcLinkData(1L, 1L, 1L);

        List<JdbcLinkData> actualResult = repository.getAll();

        assertThat(actualResult.size()).isEqualTo(1);
        assertThat(actualResult.getFirst().id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.getFirst().linkId()).isEqualTo(expectedResult.linkId());
        assertThat(actualResult.getFirst().chatId()).isEqualTo(expectedResult.chatId());
    }

    @Test
    public void getByIdTest() {
        long id = 1L;
        JdbcLinkData expectedResult = new JdbcLinkData(id, 1L, 1L);

        JdbcLinkData actualResult = repository.getById(id).get();

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.linkId()).isEqualTo(expectedResult.linkId());
        assertThat(actualResult.chatId()).isEqualTo(expectedResult.chatId());
    }

    @Test
    public void getDeletedByIdTest() {
        Optional<JdbcLinkData> actualResult = repository.getById(2);

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByIdFailTest() {
        Optional<JdbcLinkData> actualResult = repository.getById(-1);

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByChatIdTest() {
        long id = 1L;
        JdbcLinkData expectedResult = new JdbcLinkData(1L, 1L, id);

        List<JdbcLinkData> actualResult = repository.getByChatId(id);

        assertThat(actualResult.size()).isEqualTo(1);
        assertThat(actualResult.getFirst().id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.getFirst().linkId()).isEqualTo(expectedResult.linkId());
        assertThat(actualResult.getFirst().chatId()).isEqualTo(expectedResult.chatId());
    }

    @Test
    public void getDeletedByChatIdTest() {
        List<JdbcLinkData> actualResult = repository.getByChatId(2);

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByChatIdFailTest() {
        List<JdbcLinkData> actualResult = repository.getByChatId(-1);

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByLinkIdTest() {
        long id = 1L;
        JdbcLinkData expectedResult = new JdbcLinkData(1L, id, 1L);

        List<JdbcLinkData> actualResult = repository.getByLinkId(id);

        assertThat(actualResult.size()).isEqualTo(1);
        assertThat(actualResult.getFirst().id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.getFirst().linkId()).isEqualTo(expectedResult.linkId());
        assertThat(actualResult.getFirst().chatId()).isEqualTo(expectedResult.chatId());
    }

    @Test
    public void getByLinkIdWithSkipLimitTest() {
        long minId = 3L;
        long limit = 2L;
        client.sql("INSERT INTO tg_chats (chat_id, deleted) VALUES (4, false)").update();
        client.sql("INSERT INTO links_data (link_id, chat_id, deleted) VALUES (2, 1, false)")
                .update();
        client.sql("INSERT INTO links_data (link_id, chat_id, deleted) VALUES (2, 3, false)")
                .update();
        client.sql("INSERT INTO links_data (link_id, chat_id, deleted) VALUES (2, 4, false)")
                .update();
        JdbcLinkData linkData1 = new JdbcLinkData(4L, 2L, 3L);
        JdbcLinkData linkData2 = new JdbcLinkData(5L, 2L, 4L);

        List<JdbcLinkData> actualResult = repository.getByLinkId(2L, minId, limit);

        assertThat(actualResult).containsExactly(linkData1, linkData2);
    }

    @Test
    public void getDeletedByLinkIdTest() {
        List<JdbcLinkData> actualResult = repository.getByLinkId(2);

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByLinkIdFailTest() {
        List<JdbcLinkData> actualResult = repository.getByLinkId(-1);

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByChatIdLinkIdTest() {
        long linkId = 1L;
        long chatId = 1L;
        JdbcLinkData expectedResult = new JdbcLinkData(1L, linkId, chatId);

        JdbcLinkData actualResult = repository.getByChatIdLinkId(chatId, linkId).get();

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.linkId()).isEqualTo(expectedResult.linkId());
        assertThat(actualResult.chatId()).isEqualTo(expectedResult.chatId());
    }

    @Test
    public void getDeletedByChatIdLinkIdTest() {
        Optional<JdbcLinkData> actualResult = repository.getByChatIdLinkId(2L, 2L);

        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getByChatIdLinkIdFailTest() {
        Optional<JdbcLinkData> actualResult = repository.getByChatIdLinkId(-1L, -1L);

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
        JdbcLinkData linkData1 = new JdbcLinkData(1L, 1L, 1L);
        JdbcLinkData linkData2 = new JdbcLinkData(3L, 2L, 1L);

        List<JdbcLinkData> actualResult = repository.getByTagAndChatId("test", 1);

        assertThat(actualResult).containsExactly(linkData1, linkData2);
    }

    @Test
    public void createNewTest() {
        long chatId = 3L;
        long linkId = 3L;
        JdbcLinkData expectedResult = new JdbcLinkData(3L, linkId, chatId);

        repository.create(expectedResult);
        JdbcLinkData actualResult = client.sql("SELECT * FROM links_data WHERE chat_id = :chatId and link_id = :linkId")
                .param("chatId", chatId)
                .param("linkId", linkId)
                .query(JdbcLinkData.class)
                .single();

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.linkId()).isEqualTo(expectedResult.linkId());
        assertThat(actualResult.chatId()).isEqualTo(expectedResult.chatId());
    }

    @Test
    public void createDeletedTest() {
        long chatId = 2L;
        long linkId = 2L;
        JdbcLinkData expectedResult = new JdbcLinkData(2L, linkId, chatId);

        repository.create(expectedResult);
        JdbcLinkData actualResult = client.sql("SELECT * FROM links_data WHERE chat_id = :chatId and link_id = :linkId")
                .param("chatId", chatId)
                .param("linkId", linkId)
                .query(JdbcLinkData.class)
                .single();

        assertThat(actualResult.id()).isEqualTo(expectedResult.id());
        assertThat(actualResult.linkId()).isEqualTo(expectedResult.linkId());
        assertThat(actualResult.chatId()).isEqualTo(expectedResult.chatId());
    }

    @Test
    public void createExistingTest() {
        long expectedId = 1L;
        JdbcLinkData expectedResult = new JdbcLinkData(null, 1L, 1L);

        repository.create(expectedResult);

        assertThat(expectedResult.id()).isEqualTo(expectedId);
    }

    @Test
    public void deleteByIdTest() {
        long id = 1L;

        repository.deleteById(id);

        assertThat(client.sql("SELECT deleted FROM links_data WHERE id = ?")
                        .param(id)
                        .query(Boolean.class)
                        .single())
                .isTrue();
    }

    @Test
    public void deleteAlreadyDeletedByIdTest() {
        long id = 2L;

        repository.deleteById(id);

        assertThat(client.sql("SELECT deleted FROM links_data WHERE id = ?")
                        .param(id)
                        .query(Boolean.class)
                        .single())
                .isTrue();
    }

    @Test
    public void deleteTest() {
        JdbcLinkData linkData = new JdbcLinkData(1L, 1L, 1L);

        repository.deleteLinkData(linkData);

        assertThat(client.sql("SELECT deleted FROM links_data WHERE id = ?")
                        .param(linkData.id())
                        .query(Boolean.class)
                        .single())
                .isTrue();
    }

    @Test
    public void deleteAlreadyDeletedTest() {
        JdbcLinkData linkData = new JdbcLinkData(2L, 2L, 2L);

        repository.deleteLinkData(linkData);

        assertThat(client.sql("SELECT deleted FROM links_data WHERE id = ?")
                        .param(linkData.id())
                        .query(Boolean.class)
                        .single())
                .isTrue();
    }
}
