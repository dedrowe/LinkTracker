package backend.academy.scrapper.repository.tags;

import backend.academy.scrapper.entity.LinkDataTagDto;
import backend.academy.scrapper.entity.Tag;
import backend.academy.shared.dto.TagLinkCount;
import java.util.List;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@ConditionalOnProperty(havingValue = "ORM", prefix = "app", name = "access-type")
public interface JpaTagsRepository extends TagsRepository, JpaRepository<Tag, Long> {

    @Override
    @Query(value = "select t from Tag t join LinkDataToTag ldt on ldt.tagId = t.id " + "where ldt.dataId = :dataId")
    List<Tag> getAllByDataId(@Param("dataId") long dataId);

    @Override
    @Query(
            value =
                    "select new backend.academy.scrapper.entity.LinkDataTagDto(t.tag, ldt.dataId) from LinkDataToTag ldt join Tag t on t.id = ldt.tagId where ldt.dataId in :ids")
    List<LinkDataTagDto> getAllByDataIds(@Param("ids") List<Long> ids);

    @Override
    @Query(value = "select t from Tag t where t.tag in (:tags)")
    List<Tag> getAllByTagsSet(@Param("tags") Set<String> tags);

    @Override
    @Query(
            value =
                    "select new backend.academy.shared.dto.TagLinkCount(t.tag, count(*)) links_count from LinkDataToTag ldt "
                            + "join Tag t on ldt.tagId = t.id "
                            + "join JpaLinkData ld on ldt.dataId = ld.id "
                            + "join TgChat tc on ld.chatId = tc.id "
                            + "where tc.chatId = :chatId and ld.deleted = false "
                            + "group by t.tag")
    List<TagLinkCount> getTagLinksCountByChatId(@Param("chatId") long chatId);

    @Override
    @Transactional
    default void createTag(Tag tag) {
        save(tag);
        flush();
    }

    @Override
    @Modifying
    @Transactional
    @Query(value = "insert into LinkDataToTag (dataId, tagId) values (:dataId, :tagId)")
    void createRelation(@Param("dataId") long dataId, @Param("tagId") long tagId);

    @Override
    @Modifying
    @Transactional
    @Query(value = "delete from LinkDataToTag ldt where ldt.dataId = :dataId and ldt.tagId = :tagId")
    void deleteRelation(@Param("dataId") long dataId, @Param("tagId") long tagId);

    @Override
    @Modifying
    @Transactional
    @Query(value = "delete from LinkDataToTag ldt where ldt.dataId = :dataId")
    void deleteAllByDataId(@Param("dataId") long dataId);
}
