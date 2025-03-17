package backend.academy.scrapper.service;

import static backend.academy.scrapper.utils.FutureUnwrapper.unwrap;

import backend.academy.scrapper.repository.tags.TagsRepository;
import backend.academy.shared.dto.ListTagLinkCount;
import backend.academy.shared.dto.TagLinkCount;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TagService {

    private final TagsRepository tagsRepository;

    public ListTagLinkCount getTagLinksCount(long chatId) {
        List<TagLinkCount> tags = unwrap(tagsRepository.getTagLinksCountByChatId(chatId));
        return new ListTagLinkCount(tags);
    }
}
