package backend.academy.scrapper.service;

import static backend.academy.scrapper.utils.FutureUnwrapper.unwrap;

import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.entity.TgChat;
import backend.academy.scrapper.exceptionHandling.exceptions.LinkDataException;
import backend.academy.scrapper.exceptionHandling.exceptions.LinkException;
import backend.academy.scrapper.mapper.LinkMapper;
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.repository.linkdata.LinkDataRepository;
import backend.academy.shared.dto.AddLinkRequest;
import backend.academy.shared.dto.LinkResponse;
import backend.academy.shared.dto.ListLinkResponse;
import backend.academy.shared.dto.RemoveLinkRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class LinkDataService {

    private final LinkDataRepository linkDataRepository;

    private final LinkRepository linkRepository;

    private final FiltersService filtersService;

    private final TagsService tagsService;

    private final TgChatService tgChatService;

    private final LinkMapper linkMapper;

    private final LinksCheckerService updatesCheckerService;

    public ListLinkResponse getByChatId(long chatId) {
        TgChat tgChat = tgChatService.getByChatId(chatId);
        List<LinkData> links = unwrap(linkDataRepository.getByChatId(tgChat.id()));
        return createListLinkResponse(links, chatId);
    }

    @Transactional
    public LinkResponse trackLink(long chatId, AddLinkRequest request) {
        TgChat tgChat = tgChatService.getByChatId(chatId);
        updatesCheckerService.checkResource(request.link());

        Link link = linkMapper.createLink(request.link());
        unwrap(linkRepository.create(link));

        Optional<LinkData> optionalData = unwrap(linkDataRepository.getByChatIdLinkId(tgChat.id(), link.id()));
        LinkData linkData;
        if (optionalData.isPresent()) {
            linkData = optionalData.orElseThrow();
        } else {
            linkData = linkMapper.createLinkData(tgChat, link);
            unwrap(linkDataRepository.create(linkData));
        }
        CompletableFuture<Void> tags = tagsService.createAll(request.tags(), linkData);
        CompletableFuture<Void> filters = filtersService.createAll(request.filters(), linkData);
        unwrap(tags);
        unwrap(filters);

        return linkMapper.createLinkResponse(linkData, link.link(), request.tags(), request.filters());
    }

    @Transactional
    public LinkResponse untrackLink(long chatId, RemoveLinkRequest request) {
        TgChat tgChat = tgChatService.getByChatId(chatId);
        Link link = getLink(request.link());
        LinkData linkData = getLinkData(tgChat.id(), link.id());

        CompletableFuture<List<Tag>> tagsList = tagsService.getAllByDataId(linkData.id());
        CompletableFuture<List<Filter>> filtersList = filtersService.getAllByDataId(linkData.id());
        LinkResponse response = linkMapper.createLinkResponse(
                linkData,
                link.link(),
                unwrap(tagsList).stream().map(Tag::tag).toList(),
                unwrap(filtersList).stream().map(Filter::filter).toList());

        unwrap(linkDataRepository.deleteLinkData(linkData));
        return response;
    }

    public ListLinkResponse getLinksByTagAndChatId(String tag, long chatId) {
        List<LinkData> links = unwrap(linkDataRepository.getByTagAndChatId(tag, chatId));
        return createListLinkResponse(links, chatId);
    }

    private ListLinkResponse createListLinkResponse(List<LinkData> links, long chatId) {
        List<LinkResponse> responses = new ArrayList<>();
        CompletableFuture<Optional<Link>>[] futures = links.stream()
                .map(link -> linkRepository.getById(link.linkId()))
                .toArray(CompletableFuture[]::new);
        for (int i = 0; i < futures.length; ++i) {
            int finalI = i;
            Link link = unwrap(futures[finalI])
                    .orElseThrow(() -> new LinkDataException(
                            "Произошла ошибка при получении зарегистрированных ссылок",
                            String.valueOf(links.get(finalI).linkId()),
                            String.valueOf(chatId)));
            CompletableFuture<List<Tag>> tags =
                    tagsService.getAllByDataId(links.get(i).id());
            CompletableFuture<List<Filter>> filters =
                    filtersService.getAllByDataId(links.get(i).id());
            responses.add(linkMapper.createLinkResponse(
                    links.get(finalI),
                    link.link(),
                    unwrap(tags).stream().map(Tag::tag).toList(),
                    unwrap(filters).stream().map(Filter::filter).toList()));
        }
        return new ListLinkResponse(responses, responses.size());
    }

    protected Link getLink(String link) {
        return unwrap(linkRepository.getByLink(link)).orElseThrow(() -> new LinkException("Ссылка не найдена", link));
    }

    protected LinkData getLinkData(long chatId, long linkId) {
        return unwrap(linkDataRepository.getByChatIdLinkId(chatId, linkId))
                .orElseThrow(() -> new LinkDataException(
                        "Данные о ссылке не найдены", String.valueOf(linkId), String.valueOf(chatId)));
    }
}
