package backend.academy.scrapper.service;

import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.entity.LinkDataTagDto;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
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
        List<LinkData> links = linkDataRepository.getByChatId(tgChat.id());
        return createListLinkResponse(links, chatId);
    }

    @Transactional
    public LinkResponse trackLink(long chatId, AddLinkRequest request) {
        TgChat tgChat = tgChatService.getByChatId(chatId);
        updatesCheckerService.checkResource(request.link());

        Link link = linkMapper.createLink(request.link());
        linkRepository.create(link);

        Optional<LinkData> optionalData = linkDataRepository.getByChatIdLinkId(tgChat.id(), link.id());
        LinkData linkData;
        if (optionalData.isPresent()) {
            linkData = optionalData.orElseThrow();
        } else {
            linkData = linkMapper.createLinkData(tgChat, link);
            linkDataRepository.create(linkData);
        }
        tagsService.createAll(request.tags(), linkData);
        filtersService.createAll(request.filters(), linkData);

        return linkMapper.createLinkResponse(linkData, link.link(), request.tags(), request.filters());
    }

    @Transactional
    public LinkResponse untrackLink(long chatId, RemoveLinkRequest request) {
        TgChat tgChat = tgChatService.getByChatId(chatId);
        Link link = getLink(request.link());
        LinkData linkData = getLinkData(tgChat.id(), link.id());

        List<Tag> tagsList = tagsService.getAllByDataId(linkData.id());
        List<Filter> filtersList = filtersService.getAllByDataId(linkData.id());
        LinkResponse response = linkMapper.createLinkResponse(
                linkData,
                link.link(),
                tagsList.stream().map(Tag::tag).toList(),
                filtersList.stream().map(Filter::filter).toList());

        linkDataRepository.deleteLinkData(linkData);
        List<LinkData> data = linkDataRepository.getByLinkId(link.id());
        if (data.isEmpty()) {
            linkRepository.deleteById(link.id());
        }
        return response;
    }

    public ListLinkResponse getLinksByTagAndChatId(String tag, long chatId) {
        List<LinkData> links = linkDataRepository.getByTagAndChatId(tag, chatId);
        return createListLinkResponse(links, chatId);
    }

    private ListLinkResponse createListLinkResponse(List<LinkData> linksData, long chatId) {
        List<Long> ids = linksData.stream().map(LinkData::id).toList();

        List<Link> links = linkRepository.getAllByIds(
                linksData.stream().map(LinkData::linkId).toList());
        Map<Long, Link> linksMap = links.stream().collect(Collectors.toMap(Link::id, Function.identity()));

        List<Filter> filters = filtersService.getAllByDataIds(ids);
        Map<Long, List<Filter>> filtersMap = filters.stream().collect(Collectors.groupingBy(Filter::dataId));

        List<LinkDataTagDto> tags = tagsService.getAllByDataIds(ids);
        Map<Long, List<LinkDataTagDto>> tagsMap = tags.stream().collect(Collectors.groupingBy(LinkDataTagDto::dataId));

        List<LinkResponse> responses = linksData.stream()
                .map(linkData -> {
                    Link link = linksMap.get(linkData.linkId());
                    if (link == null) {
                        throw new LinkDataException(
                                "Произошла ошибка при получении зарегистрированных ссылок",
                                String.valueOf(linkData.linkId()),
                                String.valueOf(chatId));
                    }
                    List<LinkDataTagDto> linkTags = tagsMap.getOrDefault(linkData.id(), List.of());
                    List<Filter> linkFilters = filtersMap.getOrDefault(linkData.id(), List.of());
                    return linkMapper.createLinkResponse(
                            linkData,
                            link.link(),
                            linkTags.stream().map(LinkDataTagDto::tag).toList(),
                            linkFilters.stream().map(Filter::filter).toList());
                })
                .toList();

        return new ListLinkResponse(responses, responses.size());
    }

    protected Link getLink(String link) {
        return linkRepository.getByLink(link).orElseThrow(() -> new LinkException("Ссылка не найдена", link));
    }

    protected LinkData getLinkData(long chatId, long linkId) {
        return linkDataRepository
                .getByChatIdLinkId(chatId, linkId)
                .orElseThrow(() -> new LinkDataException(
                        "Данные о ссылке не найдены", String.valueOf(linkId), String.valueOf(chatId)));
    }
}
