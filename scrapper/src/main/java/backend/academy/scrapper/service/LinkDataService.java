package backend.academy.scrapper.service;

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
        List<LinkResponse> responses = new ArrayList<>();
        List<Optional<Link>> links = linksData.stream()
                .map(link -> linkRepository.getById(link.linkId()))
                .toList();
        for (int i = 0; i < links.size(); ++i) {
            int finalI = i;
            Link link = links.get(finalI)
                    .orElseThrow(() -> new LinkDataException(
                            "Произошла ошибка при получении зарегистрированных ссылок",
                            String.valueOf(linksData.get(finalI).linkId()),
                            String.valueOf(chatId)));
            List<Tag> tags = tagsService.getAllByDataId(linksData.get(i).id());
            List<Filter> filters =
                    filtersService.getAllByDataId(linksData.get(i).id());
            responses.add(linkMapper.createLinkResponse(
                    linksData.get(finalI),
                    link.link(),
                    tags.stream().map(Tag::tag).toList(),
                    filters.stream().map(Filter::filter).toList()));
        }
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
