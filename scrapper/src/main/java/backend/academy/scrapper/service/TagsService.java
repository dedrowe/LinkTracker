package backend.academy.scrapper.service;

import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.entity.LinkDataTagDto;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.repository.tags.TagsRepository;
import backend.academy.shared.dto.ListTagLinkCount;
import backend.academy.shared.dto.TagLinkCount;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TagsService {

    private final TagsRepository tagsRepository;

    public List<Tag> getAllByDataId(long dataId) {
        return tagsRepository.getAllByDataId(dataId);
    }

    public List<LinkDataTagDto> getAllByDataIds(List<Long> dataIds) {
        return tagsRepository.getAllByDataIds(dataIds);
    }

    public List<Tag> getAllByTagsSet(Set<String> tags) {
        return tagsRepository.getAllByTagsSet(tags);
    }

    public ListTagLinkCount getTagLinksCountByChatIdSync(long chatId) {
        List<TagLinkCount> tags = tagsRepository.getTagLinksCountByChatId(chatId);
        return new ListTagLinkCount(tags);
    }

    @Transactional
    public void createAll(List<String> tags, LinkData linkData) {
        List<Tag> curTags = tagsRepository.getAllByDataId(linkData.id());
        Set<String> tagSet = new HashSet<>(tags);
        Set<Tag> curTagsSet = new HashSet<>(curTags);

        for (Tag tag : Set.copyOf(curTagsSet)) {
            if (tagSet.contains(tag.tag())) {
                curTagsSet.remove(tag);
                tagSet.remove(tag.tag());
            }
        }

        List<Tag> existingTags = tagsRepository.getAllByTagsSet(tagSet);
        existingTags.forEach(tag -> tagSet.remove(tag.tag()));

        curTagsSet.forEach(tag -> tagsRepository.deleteRelation(linkData.id(), tag.id()));
        existingTags.forEach(tag -> tagsRepository.createRelation(linkData.id(), tag.id()));
        tagSet.forEach(tag -> {
            Tag newTag = new Tag(tag);
            tagsRepository.createTag(newTag);
            tagsRepository.createRelation(linkData.id(), newTag.id());
        });
    }

    public void deleteAllByDataId(long dataId) {
        tagsRepository.deleteAllByDataId(dataId);
    }
}
