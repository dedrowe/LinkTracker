package backend.academy.scrapper.service;

import static backend.academy.scrapper.utils.FutureUnwrapper.unwrap;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.exceptionHandling.exceptions.LinkDataException;
import backend.academy.scrapper.exceptionHandling.exceptions.LinkException;
import backend.academy.scrapper.mapper.LinkMapper;
import backend.academy.scrapper.repository.filters.FiltersRepository;
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.repository.linkdata.LinkDataRepository;
import backend.academy.scrapper.repository.tags.TagsRepository;
import backend.academy.shared.dto.AddLinkRequest;
import backend.academy.shared.dto.LinkResponse;
import backend.academy.shared.dto.ListLinkResponse;
import backend.academy.shared.dto.RemoveLinkRequest;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class LinkDataService {

    protected final LinkDataRepository linkDataRepository;

    protected final LinkRepository linkRepository;

    protected final FiltersRepository filtersRepository;

    protected final TagsRepository tagsRepository;

    protected final TgChatService tgChatService;

    protected final LinkMapper linkMapper;

    protected final LinksCheckerService updatesCheckerService;

    public abstract ListLinkResponse getByChatId(long chatId);

    public abstract LinkResponse trackLink(long chatId, AddLinkRequest request);

    public abstract LinkResponse untrackLink(long chatId, RemoveLinkRequest request);

    public abstract ListLinkResponse getLinksByTagAndChatId(String tag, long chatId);

    protected Link getLink(String link) {
        return unwrap(linkRepository.getByLink(link)).orElseThrow(() -> new LinkException("Ссылка не найдена", link));
    }

    protected LinkData getLinkData(long chatId, long linkId) {
        return unwrap(linkDataRepository.getByChatIdLinkId(chatId, linkId))
                .orElseThrow(() -> new LinkDataException(
                        "Данные о ссылке не найдены", String.valueOf(linkId), String.valueOf(chatId)));
    }
}
