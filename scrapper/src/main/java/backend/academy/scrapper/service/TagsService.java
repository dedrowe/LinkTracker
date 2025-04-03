package backend.academy.scrapper.service;

import static backend.academy.scrapper.utils.FutureUnwrapper.unwrap;

import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.repository.tags.TagsRepository;
import backend.academy.shared.dto.ListTagLinkCount;
import backend.academy.shared.dto.TagLinkCount;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TagsService {

    private final TagsRepository tagsRepository;

    public List<Tag> getAllByDataIdSync(long dataId) {
        return unwrap(tagsRepository.getAllByDataId(dataId));
    }

    public CompletableFuture<List<Tag>> getAllByDataId(long dataId) {
        return tagsRepository.getAllByDataId(dataId);
    }

    public List<Tag> getAllByTagsSetSync(Set<String> tags) {
        return unwrap(tagsRepository.getAllByTagsSet(tags));
    }

    public CompletableFuture<List<Tag>> getAllByTagsSet(Set<String> tags) {
        return tagsRepository.getAllByTagsSet(tags);
    }

    public ListTagLinkCount getTagLinksCountByChatIdSync(long chatId) {
        List<TagLinkCount> tags = unwrap(tagsRepository.getTagLinksCountByChatId(chatId));
        return new ListTagLinkCount(tags);
    }

    @Transactional
    public void createAllSync(List<String> tags, long dataId) {
        unwrap(createAll(tags, dataId));
    }

    @Transactional
    public CompletableFuture<Void> createAll(List<String> tags, long dataId) {
        List<Tag> curTags = unwrap(tagsRepository.getAllByDataId(dataId));
        Set<String> tagSet = new HashSet<>(tags);
        Set<Tag> curTagsSet = new HashSet<>(curTags);

        for (Tag tag : Set.copyOf(curTagsSet)) {
            if (tagSet.contains(tag.tag())) {
                curTagsSet.remove(tag);
                tagSet.remove(tag.tag());
            }
        }

        List<Tag> existingTags = unwrap(tagsRepository.getAllByTagsSet(tagSet));
        existingTags.forEach(tag -> tagSet.remove(tag.tag()));

        CompletableFuture<Void> deleteTagsFuture = CompletableFuture.allOf(curTagsSet.stream()
                .map(tag -> tagsRepository.deleteRelation(dataId, tag.id()))
                .toArray(CompletableFuture[]::new));

        CompletableFuture<Void> existingTagsFuture = CompletableFuture.allOf(existingTags.stream()
                .map(tag -> tagsRepository.createRelation(dataId, tag.id()))
                .toArray(CompletableFuture[]::new));

        CompletableFuture<Void> newTagsFuture = CompletableFuture.allOf(tagSet.stream()
                .map(tag -> {
                    Tag newTag = new Tag(tag);
                    return tagsRepository
                            .createTag(newTag)
                            .thenRunAsync(() -> tagsRepository.createRelation(dataId, newTag.id()));
                })
                .toArray(CompletableFuture[]::new));

        unwrap(CompletableFuture.allOf(deleteTagsFuture, existingTagsFuture, newTagsFuture));
        return CompletableFuture.completedFuture(null);
    }

    public void deleteAllByDataIdSync(long dataId) {
        unwrap(tagsRepository.deleteAllByDataId(dataId));
    }

    public CompletableFuture<Void> deleteAllByDataId(long dataId) {
        return tagsRepository.deleteAllByDataId(dataId);
    }
}
