package backend.academy.scrapper.service.sql;

import static backend.academy.scrapper.utils.FutureUnwrapper.unwrap;

import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.entity.TgChat;
import backend.academy.scrapper.exceptionHandling.exceptions.LinkDataException;
import backend.academy.scrapper.mapper.LinkMapper;
import backend.academy.scrapper.repository.filters.FiltersRepository;
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.repository.linkdata.LinkDataRepository;
import backend.academy.scrapper.repository.tags.TagsRepository;
import backend.academy.scrapper.service.LinkDataService;
import backend.academy.scrapper.service.LinksCheckerService;
import backend.academy.scrapper.service.TgChatService;
import backend.academy.shared.dto.AddLinkRequest;
import backend.academy.shared.dto.LinkResponse;
import backend.academy.shared.dto.ListLinkResponse;
import backend.academy.shared.dto.RemoveLinkRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnProperty(havingValue = "SQL", prefix = "app", name = "access-type")
public class SqlLinkDataService extends LinkDataService {

    @Autowired
    public SqlLinkDataService(
            LinkDataRepository linkDataRepository,
            LinkRepository linkRepository,
            FiltersRepository filtersRepository,
            TagsRepository tagsRepository,
            TgChatService tgChatService,
            LinkMapper linkMapper,
            LinksCheckerService updatesCheckerService) {
        super(
                linkDataRepository,
                linkRepository,
                filtersRepository,
                tagsRepository,
                tgChatService,
                linkMapper,
                updatesCheckerService);
    }

    @Override
    public ListLinkResponse getByChatId(long chatId) {
        TgChat tgChat = tgChatService.getByChatId(chatId);
        List<LinkData> links = unwrap(linkDataRepository.getByChatId(tgChat.id()));
        return createListLinkResponse(links, chatId);
    }

    @Override
    public LinkResponse trackLink(long chatId, AddLinkRequest request) {
        TgChat tgChat = tgChatService.getByChatId(chatId);
        updatesCheckerService.checkResource(request.link());

        Link link = linkMapper.createLink(request.link());

        Optional<Link> optionalLink = unwrap(linkRepository.getByLink(request.link()));
        if (optionalLink.isEmpty()) {
            unwrap(linkRepository.create(link));
        } else {
            link.id(optionalLink.orElseThrow().id());
        }
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

    @Override
    public LinkResponse untrackLink(long chatId, RemoveLinkRequest request) {
        TgChat tgChat = tgChatService.getByChatId(chatId);
        Link link = getLink(request.link());
        LinkData linkData = getLinkData(tgChat.id(), link.id());

        CompletableFuture<List<Tag>> tagsList = tagsRepository.getAllByDataId(linkData.id());
        CompletableFuture<List<Filter>> filtersList = filtersRepository.getAllByDataId(linkData.id());
        LinkResponse response =
                linkMapper.createLinkResponse(linkData, link.link(), unwrap(tagsList), unwrap(filtersList));

        unwrap(CompletableFuture.allOf(
                tagsRepository.deleteAllByDataId(linkData.id()),
                filtersRepository.deleteAllByDataId(linkData.id()),
                linkDataRepository.deleteLinkData(linkData)));
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
                    tagsRepository.getAllByDataId(links.get(i).id());
            CompletableFuture<List<Filter>> filters =
                    filtersRepository.getAllByDataId(links.get(i).id());
            responses.add(linkMapper.createLinkResponse(links.get(finalI), link.link(), unwrap(tags), unwrap(filters)));
        }
        return new ListLinkResponse(responses, responses.size());
    }
}
