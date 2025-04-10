package backend.academy.scrapper.repository.tags;

import backend.academy.scrapper.entity.LinkDataTagDto;
import backend.academy.scrapper.entity.Tag;
import backend.academy.shared.dto.TagLinkCount;
import java.util.List;
import java.util.Set;

public interface TagsRepository {

    List<Tag> getAllByDataId(long dataId);

    List<LinkDataTagDto> getAllByDataIds(List<Long> ids);

    List<Tag> getAllByTagsSet(Set<String> tags);

    List<TagLinkCount> getTagLinksCountByChatId(long chatId);

    void createTag(Tag tag);

    void createRelation(long dataId, long tagId);

    void deleteRelation(long dataId, long tagId);

    void deleteAllByDataId(long dataId);
}
