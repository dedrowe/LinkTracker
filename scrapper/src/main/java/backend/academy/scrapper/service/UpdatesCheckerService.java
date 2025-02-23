package backend.academy.scrapper.service;

import static backend.academy.scrapper.utils.FutureUnwrapper.unwrap;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.mapper.LinkMapper;
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.repository.linkdata.LinkDataRepository;
import backend.academy.scrapper.service.apiClient.TgBotClient;
import backend.academy.scrapper.service.apiClient.wrapper.ApiClientWrapper;
import backend.academy.shared.exceptions.BaseException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class UpdatesCheckerService {

    private final LinkDispatcher linkDispatcher;

    private final LinkDataRepository linkDataRepository;

    private final LinkRepository linkRepository;

    private final LinkMapper linkMapper;

    private final TgBotClient tgBotClient;

    private final TgChatService tgChatService;

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    public void checkUpdates() {
        List<Link> links = unwrap(linkRepository.getAll());

        for (Link link : links) {
            URI uri = URI.create(link.link());
            ApiClientWrapper client = linkDispatcher.dispatchLink(uri);
            LocalDateTime lastUpdate = client.getLastUpdate(uri);
            if (lastUpdate.isAfter(link.lastUpdate())) {
                link.lastUpdate(lastUpdate);
                unwrap(linkRepository.update(link));

                List<LinkData> linkDataList = unwrap(linkDataRepository.getByLinkId(link.id()));
                List<Long> chatIds = new ArrayList<>();
                for (LinkData linkData : linkDataList) {
                    chatIds.add(tgChatService.getById(linkData.chatId()).chatId());
                }
                tgBotClient.sendUpdates(linkMapper.createLinkUpdate(
                        link.id(), link.link(), "Получено обновление по ссылке " + link.link(), chatIds));
            }
        }
    }

    @SuppressWarnings("PMD.UnusedLocalVariable")
    public void checkResource(String url) {
        try {
            URI uri = new URI(url);
            uri.toURL();
            ApiClientWrapper client = linkDispatcher.dispatchLink(uri);
            client.getLastUpdate(uri);
        } catch (MalformedURLException | URISyntaxException | IllegalArgumentException e) {
            String exceptionMessage = "Ошибка в синтаксисе ссылки";
            BaseException ex = new BaseException(exceptionMessage, e);
            try (var var = MDC.putCloseable("url", url)) {
                log.info(exceptionMessage, ex);
            }
            throw ex;
        }
    }
}
