package backend.academy.scrapper.repository.tags;

import backend.academy.scrapper.entity.Tag;
import backend.academy.shared.dto.TagLinkCount;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
@ConditionalOnProperty(havingValue = "SQL", prefix = "app", name = "access-type")
public class JdbcTagsRepository implements TagsRepository {

    private final JdbcClient jdbcClient;

    @Async
    @Override
    public CompletableFuture<List<Tag>> getAllByDataId(long dataId) {
        String query =
                "select tags.id, tags.tag from tags join links_data_to_tags on links_data_to_tags.tag_id = tags.id "
                        + "where links_data_to_tags.data_id = :dataId";

        return CompletableFuture.completedFuture(
                jdbcClient.sql(query).param("dataId", dataId).query(Tag.class).list());
    }

    @Async
    @Override
    public CompletableFuture<List<Tag>> getAllByTagsSet(Set<String> tags) {
        if (tags.isEmpty()) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        String query = "select * from tags where tag in (:tags)";
        return CompletableFuture.completedFuture(
                jdbcClient.sql(query).param("tags", tags).query(Tag.class).list());
    }

    @Async
    @Override
    public CompletableFuture<List<TagLinkCount>> getTagLinksCountByChatId(long chatId) {
        String query = "select tags.tag, count(*) as links_count " + "from links_data_to_tags "
                + "join tags on links_data_to_tags.tag_id = tags.id "
                + "join links_data on links_data_to_tags.data_id = links_data.id "
                + "join tg_chats on links_data.chat_id = tg_chats.id "
                + "where tg_chats.chat_id = :chatId and links_data.deleted = false "
                + "group by tags.tag";

        return CompletableFuture.completedFuture(jdbcClient
                .sql(query)
                .param("chatId", chatId)
                .query(TagLinkCount.class)
                .list());
    }

    @Async
    @Override
    public CompletableFuture<Void> createTag(Tag tag) {
        String query = "insert into tags (tag) values (:tag) returning id";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql(query).param("tag", tag.tag()).update(keyHolder);
        tag.id(keyHolder.getKeyAs(Long.class));

        return CompletableFuture.completedFuture(null);
    }

    @Async
    @Override
    public CompletableFuture<Void> createRelation(long dataId, long tagId) {
        String query = "insert into links_data_to_tags (data_id, tag_id) values (:dataId, :tagId)";

        jdbcClient.sql(query).param("dataId", dataId).param("tagId", tagId).update();

        return CompletableFuture.completedFuture(null);
    }

    @Async
    @Override
    public CompletableFuture<Void> deleteRelation(long dataId, long tagId) {
        String query = "delete from links_data_to_tags where data_id = :dataId and tag_id = :tagId";

        jdbcClient.sql(query).param("dataId", dataId).param("tagId", tagId).update();
        return CompletableFuture.completedFuture(null);
    }

    @Async
    @Override
    public CompletableFuture<Void> deleteAllByDataId(long dataId) {
        String query = "delete from links_data_to_tags where data_id = :dataId";

        jdbcClient.sql(query).param("dataId", dataId).update();

        return CompletableFuture.completedFuture(null);
    }
}
