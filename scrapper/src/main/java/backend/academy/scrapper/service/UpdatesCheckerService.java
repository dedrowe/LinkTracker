package backend.academy.scrapper.service;

import static backend.academy.scrapper.utils.FutureUnwrapper.unwrap;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.entity.TgChat;
import backend.academy.scrapper.exceptionHandling.exceptions.TgChatException;
import backend.academy.scrapper.exceptionHandling.exceptions.WrongServiceException;
import backend.academy.scrapper.mapper.LinkMapper;
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.repository.linkdata.LinkDataRepository;
import backend.academy.scrapper.repository.tgchat.TgChatRepository;
import backend.academy.scrapper.service.apiClient.TgBotClient;
import backend.academy.scrapper.service.apiClient.wrapper.ApiClientWrapper;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final TgChatRepository tgChatRepository;

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
                CompletableFuture<Optional<TgChat>>[] futures = linkDataList.stream()
                        .map(linkData -> tgChatRepository.getById(linkData.chatId()))
                        .toArray(CompletableFuture[]::new);
                for (int i = 0; i < futures.length; i++) {
                    int finalI = i;
                    chatIds.add(unwrap(futures[i])
                            .orElseThrow(() -> new TgChatException(
                                    "Чат с таким id не зарегистрирован", String.valueOf(linkDataList.get(finalI))))
                            .chatId());
                }
                tgBotClient.sendUpdates(linkMapper.createLinkUpdate(
                        link.id(), link.link(), "Получено обновление по ссылке " + link.link(), chatIds));
            }
        }
    }

    public void checkResource(String url) {
        try {
            URI uri = new URI(url);
            uri.toURL();
            ApiClientWrapper client = linkDispatcher.dispatchLink(uri);
            client.getLastUpdate(uri);
        } catch (MalformedURLException | URISyntaxException | IllegalArgumentException e) {
            throw new WrongServiceException("Ошибка в синтаксисе ссылки", url, e);
        }
    }
}
