package backend.academy.scrapper.service.orm;

import static backend.academy.scrapper.utils.FutureUnwrapper.unwrap;

import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.entity.TgChat;
import backend.academy.scrapper.mapper.LinkMapper;
import backend.academy.scrapper.repository.filters.FiltersRepository;
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.repository.linkdata.LinkDataRepository;
import backend.academy.scrapper.repository.tags.TagsRepository;
import backend.academy.scrapper.service.LinkDataService;
import backend.academy.scrapper.service.TgChatService;
import backend.academy.scrapper.service.sql.SqlUpdatesCheckerService;
import backend.academy.shared.dto.AddLinkRequest;
import backend.academy.shared.dto.LinkResponse;
import backend.academy.shared.dto.ListLinkResponse;
import backend.academy.shared.dto.RemoveLinkRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnProperty(havingValue = "ORM", prefix = "app", name = "access-type")
public class OrmLinkDataService extends LinkDataService {

    @Autowired
    public OrmLinkDataService(
            LinkDataRepository linkDataRepository,
            LinkRepository linkRepository,
            FiltersRepository filtersRepository,
            TagsRepository tagsRepository,
            TgChatService tgChatService,
            LinkMapper linkMapper,
            SqlUpdatesCheckerService updatesCheckerService) {
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
        return createListLinkResponse(links);
    }

    @Override
    public LinkResponse trackLink(long chatId, AddLinkRequest request) {
        TgChat tgChat = tgChatService.getByChatId(chatId);
        updatesCheckerService.checkResource(request.link());

        Set<String> newTags = new HashSet<>(request.tags());
        CompletableFuture<List<Tag>> tagsFuture = tagsRepository.getAllByTagsSet(newTags);
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

        linkData.filters().clear();
        linkData.tags().clear();
        request.filters().forEach(f -> linkData.filters().add(new Filter(linkData, f)));
        unwrap(tagsFuture).forEach(tag -> {
            if (newTags.contains(tag.tag())) {
                linkData.tags().add(tag);
                newTags.remove(tag.tag());
            }
        });
        newTags.forEach(t -> linkData.tags().add(new Tag(t)));
        unwrap(linkDataRepository.create(linkData));
        return linkMapper.createLinkResponse(linkData, linkData.link().link(), linkData.tags(), linkData.filters());
    }

    @Override
    public LinkResponse untrackLink(long chatId, RemoveLinkRequest request) {
        TgChat tgChat = tgChatService.getByChatId(chatId);
        Link link = getLink(request.link());
        LinkData linkData = getLinkData(tgChat.id(), link.id());
        LinkResponse response =
                linkMapper.createLinkResponse(linkData, link.link(), linkData.tags(), linkData.filters());

        unwrap(linkDataRepository.deleteLinkData(linkData));

        return response;
    }

    @Override
    public ListLinkResponse getLinksByTagAndChatId(String tag, long chatId) {
        List<LinkData> links = unwrap(linkDataRepository.getByTagAndChatId(tag, chatId));
        return createListLinkResponse(links);
    }

    private ListLinkResponse createListLinkResponse(List<LinkData> links) {
        List<LinkResponse> responses = links.stream()
                .map(linkData -> linkMapper.createLinkResponse(
                        linkData, linkData.link().link(), linkData.tags(), linkData.filters()))
                .toList();
        return new ListLinkResponse(responses, responses.size());
    }
}
