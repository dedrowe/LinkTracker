package backend.academy.scrapper.repository.tags;

import backend.academy.scrapper.entity.LinkDataToTag;
import backend.academy.scrapper.entity.Tag;
import backend.academy.shared.dto.TagLinkCount;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@ConditionalOnProperty(havingValue = "ORM", prefix = "app", name = "access-type")
public interface JpaTagsRepository extends TagsRepository, Repository<Tag, Long> {

    @Override
    @Async
    @Transactional
    default CompletableFuture<Void> createAll(List<String> tags, long dataId) {
        createAllSync(tags, dataId);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    default CompletableFuture<List<Tag>> getAllByDataId(long dataId) {
        return CompletableFuture.completedFuture(getAllTagsByDataIdSync(dataId));
    }

    @Override
    @Async
    @Transactional
    default CompletableFuture<Void> deleteAllByDataId(long dataId) {
        deleteAllByDataIdSync(dataId);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    default CompletableFuture<List<TagLinkCount>> getTagLinksCountByChatId(long chatId) {
        return CompletableFuture.completedFuture(getTagLinksCountByChatIdSync(chatId));
    }

    @Query(value = "select t from Tag t " +
        "join LinkDataToTag ldt on ldt.tagId = t.id " +
        "where ldt.dataId = :dataId and ldt.deleted = false")
    List<Tag> getAllTagsByDataIdSync(@Param("dataId") long dataId);

    @Query(value = "select t from Tag t where t.tag in (:tags)")
    List<Tag> getAllTagsByTagsSetSync(@Param("tags") Set<String> tags);

    @Query(value = "select ldt from LinkDataToTag ldt where ldt.dataId = :dataId and ldt.tagId = :tagId")
    Optional<LinkDataToTag> getRelationByDataIdAndTagIdSync(@Param("dataId") long dataId, @Param("tagId") long tagId);

    @Query(value = "select t from Tag t where t.tag = :tag")
    Optional<Tag> getTagByTag(@Param("tag") String tag);

    @Transactional
    default void createAllSync(List<String> tags, long dataId) {
        List<Tag> curTags = getAllTagsByDataIdSync(dataId);
        Set<String> tagSet = new HashSet<>(tags);
        Set<Tag> curTagsSet = new HashSet<>(curTags);

        for (Tag tag : Set.copyOf(curTagsSet)) {
            if (tagSet.contains(tag.tag())) {
                curTagsSet.remove(tag);
                tagSet.remove(tag.tag());
            }
        }
        for (Tag tag : curTagsSet) {
            deleteRelationSync(dataId, tag.id());
        }
        getAllTagsByTagsSetSync(tagSet).forEach(tag -> {
            Optional<LinkDataToTag> data = getRelationByDataIdAndTagIdSync(dataId, tag.id());
            if (data.isPresent()) {
                restoreRelationSync(data.orElseThrow().id());
            } else {
                createRelationSync(dataId, tag.id());
            }
            tagSet.remove(tag.tag());
        });
        for (String tag : tagSet) {
            createTagSync(tag);
            createRelationSync(dataId, getTagByTag(tag).orElseThrow().id());
        }
    }

    @Modifying
    @Transactional
    @Query(value = "insert into LinkDataToTag (dataId, tagId) values (:dataId, :tagId)")
    void createRelationSync(@Param("dataId") long dataId, @Param("tagId") Long tagId);

    @Modifying
    @Transactional
    @Query(value = "update LinkDataToTag ldt set ldt.deleted = false where ldt.id = :id")
    void restoreRelationSync(@Param("id") long id);

    @Modifying
    @Transactional
    @Query(value = "insert into Tag (tag) values (:tag)")
    void createTagSync(@Param("tag") String tag);

    @Modifying
    @Transactional
    @Query(value = "update LinkDataToTag ldt set ldt.deleted = true where ldt.dataId = :dataId")
    void deleteAllByDataIdSync(@Param("dataId") long dataId);

    @Query(value = "select new backend.academy.shared.dto.TagLinkCount(t.tag, count(*)) links_count from LinkDataToTag ldt " +
        "join Tag t on ldt.tagId = t.id " +
        "join LinkData ld on ldt.dataId = ld.id " +
        "join TgChat tc on ld.chatId = tc.id " +
        "where tc.chatId = :chatId and ldt.deleted = false " +
        "group by t.tag")
    List<TagLinkCount> getTagLinksCountByChatIdSync(@Param("chatId") long chatId);

    @Modifying
    @Transactional
    @Query(value = "update LinkDataToTag ldt set ldt.deleted = true where ldt.dataId = :dataId and ldt.tagId = :tagId")
    void deleteRelationSync(@Param("dataId") long dataId, @Param("tagId") long tagId);
}
