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
import backend.academy.scrapper.repository.filters.FiltersRepository;
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.repository.linkdata.LinkDataRepository;
import backend.academy.scrapper.repository.tags.TagsRepository;
import backend.academy.shared.dto.AddLinkRequest;
import backend.academy.shared.dto.LinkResponse;
import backend.academy.shared.dto.ListLinkResponse;
import backend.academy.shared.dto.RemoveLinkRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class LinkDataService {

    private final LinkDataRepository linkDataRepository;

    private final LinkRepository linkRepository;

    private final FiltersRepository filtersRepository;

    private final TagsRepository tagsRepository;

    private final TgChatService tgChatService;

    private final LinkMapper linkMapper;

    private final UpdatesCheckerService updatesCheckerService;

    public ListLinkResponse getByChatId(long chatId) {
        TgChat tgChat = tgChatService.getByChatId(chatId);
        List<LinkData> links = unwrap(linkDataRepository.getByChatId(tgChat.id()));
        return createListLinkResponse(links, links.size());
    }

    public LinkResponse trackLink(long chatId, AddLinkRequest request) {
        TgChat tgChat = tgChatService.getByChatId(chatId);
        updatesCheckerService.checkResource(request.link());

        Optional<Link> optionalLink = unwrap(linkRepository.getByLink(request.link()));
        if (optionalLink.isEmpty()) {
            unwrap(linkRepository.create(new Link(request.link())));
            optionalLink = unwrap(linkRepository.getByLink(request.link()));
        }
        Link link = optionalLink.orElseThrow();

        LinkData linkData = linkMapper.createLinkData(tgChat.id(), link.id());
        Optional<LinkData> optionalLinkData = unwrap(linkDataRepository.getByChatIdLinkId(tgChat.id(), link.id()));
        if (optionalLinkData.isEmpty()) {
            unwrap(linkDataRepository.create(linkData));
        } else {
            linkData = optionalLinkData.orElseThrow();
        }
        CompletableFuture<Void> tags = tagsRepository.createAll(request.tags(), linkData.id());
        CompletableFuture<Void> filters = filtersRepository.createAll(request.filters(), linkData.id());
        unwrap(tags);
        unwrap(filters);

        CompletableFuture<List<Tag>> tagsList = tagsRepository.getAllByDataId(linkData.id());
        CompletableFuture<List<Filter>> filtersList = filtersRepository.getAllByDataId(linkData.id());
        return linkMapper.createLinkResponse(linkData, link.link(), unwrap(tagsList), unwrap(filtersList));
    }

    public LinkResponse untrackLink(long chatId, RemoveLinkRequest request) {
        TgChat tgChat = tgChatService.getByChatId(chatId);
        Link link = unwrap(linkRepository.getByLink(request.link()))
                .orElseThrow(() -> new LinkException("Ссылка не найдена", request.link()));
        LinkData linkData = unwrap(linkDataRepository.getByChatIdLinkId(tgChat.id(), link.id()))
                .orElseThrow(() -> new LinkDataException(
                        "Данные о ссылке не найдены", String.valueOf(link.id()), String.valueOf(tgChat.id())));
        unwrap(linkDataRepository.delete(linkData));

        CompletableFuture<List<Tag>> tagsList = tagsRepository.getAllByDataId(linkData.id());
        CompletableFuture<List<Filter>> filtersList = filtersRepository.getAllByDataId(linkData.id());
        LinkResponse response =
                linkMapper.createLinkResponse(linkData, link.link(), unwrap(tagsList), unwrap(filtersList));

        CompletableFuture<Void> tags = tagsRepository.deleteAllByDataId(linkData.id());
        CompletableFuture<Void> filters = filtersRepository.deleteAllByDataId(linkData.id());
        unwrap(tags);
        unwrap(filters);
        return response;
    }

    public ListLinkResponse getLinksByTagAndChatId(String tag, long chatId) {
        List<LinkData> links = unwrap(linkDataRepository.getByTagAndChatId(tag, chatId));
        return createListLinkResponse(links, links.size());
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
                    tagsRepository.getAllByDataId(links.get(i).id());
            CompletableFuture<List<Filter>> filters =
                    filtersRepository.getAllByDataId(links.get(i).id());
            responses.add(linkMapper.createLinkResponse(links.get(finalI), link.link(), unwrap(tags), unwrap(filters)));
        }
        return new ListLinkResponse(responses, responses.size());
    }
}
