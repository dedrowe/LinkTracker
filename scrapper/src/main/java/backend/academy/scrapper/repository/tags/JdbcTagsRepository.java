package backend.academy.scrapper.repository.tags;

import backend.academy.scrapper.entity.Tag;
import backend.academy.shared.dto.TagLinkCount;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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
        return CompletableFuture.completedFuture(getAllTagsByDataId(dataId));
    }

    @Async
    @Override
    public CompletableFuture<List<Tag>> getAllByTagsSet(Set<String> tags) {
        return CompletableFuture.completedFuture(getAllTagsByTagsSet(tags));
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
    public CompletableFuture<Void> createAll(List<String> tags, long dataId) {
        List<Tag> curTags = getAllTagsByDataId(dataId);
        Set<String> tagSet = new HashSet<>(tags);
        Set<Tag> curTagsSet = new HashSet<>(curTags);

        for (Tag tag : Set.copyOf(curTagsSet)) {
            if (tagSet.contains(tag.tag())) {
                curTagsSet.remove(tag);
                tagSet.remove(tag.tag());
            }
        }
        for (Tag tag : curTagsSet) {
            deleteRelation(dataId, tag.id());
        }
        getAllTagsByTagsSet(tagSet).forEach(tag -> {
            createRelation(dataId, tag.id());
            tagSet.remove(tag.tag());
        });
        for (String tag : tagSet) {
            createRelation(dataId, createTag(tag));
        }

        return CompletableFuture.completedFuture(null);
    }

    @Async
    @Override
    public CompletableFuture<Void> deleteAllByDataId(long dataId) {
        String query = "delete from links_data_to_tags where data_id = :dataId";

        jdbcClient.sql(query).param("dataId", dataId).update();

        return CompletableFuture.completedFuture(null);
    }

    private List<Tag> getAllTagsByDataId(long dataId) {
        return jdbcClient
                .sql(
                        "select tags.id, tags.tag from tags join links_data_to_tags on links_data_to_tags.tag_id = tags.id "
                                + "where links_data_to_tags.data_id = :dataId")
                .param("dataId", dataId)
                .query(Tag.class)
                .list();
    }

    private List<Tag> getAllTagsByTagsSet(Set<String> tags) {
        if (tags.isEmpty()) {
            return List.of();
        }
        return jdbcClient
                .sql("select * from tags where tag in (:tags)")
                .param("tags", tags)
                .query(Tag.class)
                .list();
    }

    private void createRelation(long dataId, Long tagId) {
        String query = "insert into links_data_to_tags (data_id, tag_id) values (:dataId, :tagId)";

        jdbcClient.sql(query).param("dataId", dataId).param("tagId", tagId).update();
    }

    private long createTag(String tag) {
        String query = "insert into tags (tag) values (:tag) returning id";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql(query).param("tag", tag).update(keyHolder);
        return Objects.requireNonNull(keyHolder.getKeyAs(Long.class));
    }

    private void deleteRelation(long dataId, long tagId) {
        String query = "delete from links_data_to_tags where data_id = :dataId and tag_id = :tagId";
        System.out.println(dataId);
        System.out.println(tagId);

        int res = jdbcClient
                .sql(query)
                .param("dataId", dataId)
                .param("tagId", tagId)
                .update();
        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" + res);
    }
}
