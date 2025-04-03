package backend.academy.scrapper.repository.tags;

import backend.academy.scrapper.entity.Tag;
import backend.academy.shared.dto.TagLinkCount;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

@ConditionalOnProperty(havingValue = "ORM", prefix = "app", name = "access-type")
public interface JpaTagsRepository extends TagsRepository, CrudRepository<Tag, Long> {

    @Override
    @Async
    default CompletableFuture<List<Tag>> getAllByDataId(long dataId) {
        return CompletableFuture.completedFuture(getAllByDataIdSync(dataId));
    }

    @Override
    @Async
    default CompletableFuture<List<Tag>> getAllByTagsSet(Set<String> tags) {
        return CompletableFuture.completedFuture(getAllByTagsSetSync(tags));
    }

    @Override
    @Async
    default CompletableFuture<List<TagLinkCount>> getTagLinksCountByChatId(long chatId) {
        return CompletableFuture.completedFuture(getTagLinksCountByChatIdSync(chatId));
    }

    @Override
    @Async
    @Transactional
    default CompletableFuture<Void> createTag(Tag tag) {
        createTagSync(tag);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Transactional
    default CompletableFuture<Void> createRelation(long dataId, long tagId) {
        createRelationSync(dataId, tagId);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Transactional
    default CompletableFuture<Void> deleteRelation(long dataId, long tagId) {
        deleteRelationSync(dataId, tagId);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    @Transactional
    default CompletableFuture<Void> deleteAllByDataId(long dataId) {
        deleteAllByDataIdSync(dataId);
        return CompletableFuture.completedFuture(null);
    }

    @Query(value = "select t from Tag t join LinkDataToTag ldt on ldt.tagId = t.id " + "where ldt.dataId = :dataId")
    List<Tag> getAllByDataIdSync(@Param("dataId") long dataId);

    @Query(value = "select t from Tag t where t.tag in (:tags)")
    List<Tag> getAllByTagsSetSync(@Param("tags") Set<String> tags);

    @Query(
            value =
                    "select new backend.academy.shared.dto.TagLinkCount(t.tag, count(*)) links_count from LinkDataToTag ldt "
                            + "join Tag t on ldt.tagId = t.id "
                            + "join LinkData ld on ldt.dataId = ld.id "
                            + "join TgChat tc on ld.chatId = tc.id "
                            + "where tc.chatId = :chatId and ld.deleted = false "
                            + "group by t.tag")
    List<TagLinkCount> getTagLinksCountByChatIdSync(@Param("chatId") long chatId);

    @Transactional
    default void createTagSync(Tag tag) {
        save(tag);
    }

    @Modifying
    @Transactional
    @Query(value = "insert into LinkDataToTag (dataId, tagId) values (:dataId, :tagId)")
    void createRelationSync(@Param("dataId") long dataId, @Param("tagId") long tagId);

    @Modifying
    @Transactional
    @Query(value = "delete from LinkDataToTag ldt where ldt.dataId = :dataId and ldt.tagId = :tagId")
    void deleteRelationSync(@Param("dataId") long dataId, @Param("tagId") long tagId);

    @Modifying
    @Transactional
    @Query(value = "delete from LinkDataToTag ldt where ldt.dataId = :dataId")
    void deleteAllByDataIdSync(@Param("dataId") long dataId);
}
