package backend.academy.scrapper.service;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkData;
import backend.academy.shared.exceptions.BaseException;
import backend.academy.scrapper.mapper.LinkMapper;
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.repository.linkdata.LinkDataRepository;
import backend.academy.scrapper.service.apiClient.TgBotClient;
import backend.academy.scrapper.service.apiClient.wrapper.ApiClientWrapper;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class UpdatesCheckerService {

    private final LinkDispatcher linkDispatcher;

    private final LinkDataRepository linkDataRepository;

    private final LinkRepository linkRepository;

    private final LinkMapper linkMapper;

    private final TgBotClient tgBotClient;

    private final TgChatService tgChatService;

    public UpdatesCheckerService(
        LinkDispatcher linkDispatcher, LinkDataRepository linkDataRepository, LinkRepository linkRepository, LinkMapper linkMapper, TgBotClient tgBotClient,
        TgChatService tgChatService) {
        this.linkDispatcher = linkDispatcher;
        this.linkDataRepository = linkDataRepository;
        this.linkRepository = linkRepository;
        this.linkMapper = linkMapper;
        this.tgBotClient = tgBotClient;
        this.tgChatService = tgChatService;
    }

    @Scheduled(fixedRate = 5000)
    public void checkUpdates() {
        List<Link> links = linkRepository.getAll();

        for (Link link : links) {
            URI uri = URI.create(link.link());
            ApiClientWrapper client = linkDispatcher.dispatchLink(uri);
            LocalDateTime lastUpdate = client.getLastUpdate(uri);
            if (lastUpdate.isAfter(link.lastUpdate())) {
                link.lastUpdate(lastUpdate);
                linkRepository.update(link);

                List<LinkData> linkDataList = linkDataRepository.getByLinkId(link.id());
                List<Long> chatIds = new ArrayList<>();
                for (LinkData linkData : linkDataList) {
                    chatIds.add(tgChatService.getById(linkData.chatId()).chatId());
                }
                tgBotClient.sendUpdates(linkMapper.createLinkUpdate(link.id(), link.link(), "Update for link " + link.link(), chatIds));
            }
        }
    }

    public void checkResource(String url) {
        try {
            URI uri = new URI(url);
            ApiClientWrapper client = linkDispatcher.dispatchLink(uri);
            client.getLastUpdate(uri);
        } catch (URISyntaxException e) {
            throw new BaseException("Ошибка в синтаксисе ссылки", e);
        }
    }
}
