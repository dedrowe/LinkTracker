package backend.academy.scrapper.repository.tags;

import backend.academy.scrapper.entity.Tag;
import backend.academy.shared.dto.TagLinkCount;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;

public interface TagsRepository {
    @Async
    CompletableFuture<Void> createAll(List<String> tags, long dataId);

    @Async
    CompletableFuture<List<Tag>> getAllByDataId(long dataId);

    @Async
    CompletableFuture<List<Tag>> getAllByTagsSet(Set<String> tags);

    @Async
    CompletableFuture<Void> deleteAllByDataId(long dataId);

    @Async
    CompletableFuture<List<TagLinkCount>> getTagLinksCountByChatId(long chatId);
}
