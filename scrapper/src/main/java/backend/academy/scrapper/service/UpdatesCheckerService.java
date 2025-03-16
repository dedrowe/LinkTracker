package backend.academy.scrapper.service;

import static backend.academy.scrapper.utils.FutureUnwrapper.unwrap;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.entity.TgChat;
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
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class UpdatesCheckerService {

    private static final int DEFAULT_BATCH_SIZE = 200;

    private static final int DEFAULT_THREAD_POOL_SIZE = 4;

    private final int batchSize;

    private final int threadPoolSize;

    private final LinkDispatcher linkDispatcher;

    private final LinkDataRepository linkDataRepository;

    private final LinkRepository linkRepository;

    private final LinkMapper linkMapper;

    private final TgBotClient tgBotClient;

    private final TgChatRepository tgChatRepository;

    @Autowired
    public UpdatesCheckerService(
            LinkDispatcher linkDispatcher,
            LinkDataRepository linkDataRepository,
            LinkRepository linkRepository,
            LinkMapper linkMapper,
            TgBotClient tgBotClient,
            TgChatRepository tgChatRepository) {
        this.linkDispatcher = linkDispatcher;
        this.linkDataRepository = linkDataRepository;
        this.linkRepository = linkRepository;
        this.linkMapper = linkMapper;
        this.tgBotClient = tgBotClient;
        this.tgChatRepository = tgChatRepository;
        this.batchSize = DEFAULT_BATCH_SIZE;
        this.threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    public void checkUpdates() {
        try (ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize)) {
            long linksBatchesCount = 0;
            while (true) {
                List<Link> links = unwrap(linkRepository.getAllNotChecked(
                        batchSize * linksBatchesCount, batchSize, LocalDateTime.now(ZoneOffset.UTC)));
                if (links.isEmpty()) {
                    break;
                }

                List<Future<Void>> results = executorService.invokeAll(links.stream()
                        .map(link -> (Callable<Void>) () -> checkUpdatesForLink(link))
                        .toList());
                for (int i = 0; i < results.size(); i++) {
                    try {
                        results.get(i).get();
                    } catch (ExecutionException e) {
                        MDC.put("link", links.get(i).link());
                        log.error("Произошла ошибка при получении обновлений", e.getCause());
                        MDC.remove("link");
                    }
                }

                ++linksBatchesCount;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Void checkUpdatesForLink(Link link) {
        URI uri = URI.create(link.link());
        ApiClientWrapper client = linkDispatcher.dispatchLink(uri);

        long linksDataBatchesCount = 0;
        Optional<String> description = Optional.empty();
        while (true) {
            List<LinkData> linkDataList =
                unwrap(linkDataRepository.getByLinkId(link.id(), linksDataBatchesCount * batchSize, batchSize));
            if (linkDataList.isEmpty()) {
                break;
            }
            if (linksDataBatchesCount == 0) {
                description = client.getLastUpdate(uri, link.lastUpdate());
            }
            if (description.isPresent()) {
                sendUpdatesForLink(link.id(), link.link(), description.orElseThrow(), linkDataList);
            }
            ++linksDataBatchesCount;
        }
        link.lastUpdate(LocalDateTime.now(ZoneOffset.UTC));
        unwrap(linkRepository.update(link));
        return null;
    }

    private void sendUpdatesForLink(long linkId, String link, String description, List<LinkData> linkDataList) {
        List<Long> chatIds = new ArrayList<>();
        CompletableFuture<Optional<TgChat>>[] futures = linkDataList.stream()
                .map(linkData -> tgChatRepository.getById(linkData.chatId()))
                .toArray(CompletableFuture[]::new);
        for (int i = 0; i < futures.length; i++) {
            int finalI = i;
            unwrap(futures[i]).ifPresentOrElse(tgChat -> chatIds.add(tgChat.chatId()), () -> {
                try (var ignored = MDC.putCloseable(
                        "id", String.valueOf(linkDataList.get(finalI).chatId()))) {
                    log.warn("Чат не найден");
                }
            });
        }
        tgBotClient.sendUpdates(linkMapper.createLinkUpdate(
                linkId, link, "Получено обновление по ссылке " + link + "\n" + description, chatIds));
    }

    public void checkResource(String url) {
        try {
            URI uri = new URI(url);
            uri.toURL();
            ApiClientWrapper client = linkDispatcher.dispatchLink(uri);
            client.checkResource(uri);
        } catch (MalformedURLException | URISyntaxException | IllegalArgumentException e) {
            throw new WrongServiceException("Ошибка в синтаксисе ссылки", url, e);
        }
    }
}
