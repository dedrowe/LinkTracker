package backend.academy.scrapper.service.sql;

import static backend.academy.scrapper.utils.FutureUnwrapper.unwrap;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.entity.TgChat;
import backend.academy.scrapper.mapper.LinkMapper;
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.repository.linkdata.LinkDataRepository;
import backend.academy.scrapper.repository.tgchat.TgChatRepository;
import backend.academy.scrapper.service.LinkDispatcher;
import backend.academy.scrapper.service.LinksCheckerService;
import backend.academy.scrapper.service.apiClient.TgBotClient;
import backend.academy.scrapper.service.apiClient.wrapper.ApiClientWrapper;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@ConditionalOnProperty(havingValue = "SQL", prefix = "app", name = "access-type")
public class SqlLinksCheckerService extends LinksCheckerService {

    private static final int DEFAULT_BATCH_SIZE = 200;

    private final int batchSize;

    private final LinkDataRepository linkDataRepository;

    private final LinkRepository linkRepository;

    private final LinkMapper linkMapper;

    private final TgBotClient tgBotClient;

    private final TgChatRepository tgChatRepository;

    public SqlLinksCheckerService(
            LinkDispatcher linkDispatcher,
            int batchSize,
            LinkDataRepository linkDataRepository,
            LinkRepository linkRepository,
            LinkMapper linkMapper,
            TgBotClient tgBotClient,
            TgChatRepository tgChatRepository) {
        this.linkDispatcher = linkDispatcher;
        this.batchSize = batchSize;
        this.linkDataRepository = linkDataRepository;
        this.linkRepository = linkRepository;
        this.linkMapper = linkMapper;
        this.tgBotClient = tgBotClient;
        this.tgChatRepository = tgChatRepository;
    }

    @Autowired
    public SqlLinksCheckerService(
            LinkDispatcher linkDispatcher,
            LinkDataRepository linkDataRepository,
            LinkRepository linkRepository,
            LinkMapper linkMapper,
            TgBotClient tgBotClient,
            TgChatRepository tgChatRepository,
            ScrapperConfig config) {
        this.linkDispatcher = linkDispatcher;
        this.linkDataRepository = linkDataRepository;
        this.linkRepository = linkRepository;
        this.linkMapper = linkMapper;
        this.tgBotClient = tgBotClient;
        this.tgChatRepository = tgChatRepository;
        batchSize = config.updatesChecker().batchSize();
    }

    @Override
    @Transactional
    public void checkUpdatesForLink(Link link) {
        URI uri = URI.create(link.link());
        ApiClientWrapper client = linkDispatcher.dispatchLink(uri);

        long linksDataBatchesCount = 0;
        Optional<String> description = Optional.empty();
        long minId = -1;
        while (true) {
            List<LinkData> linkDataList = unwrap(linkDataRepository.getByLinkId(link.id(), minId, batchSize));
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
            minId = linkDataList.getLast().id();
        }
        link.lastUpdate(LocalDateTime.now(ZoneOffset.UTC));
        unwrap(linkRepository.update(link));
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
        if (chatIds.isEmpty()) {
            return;
        }
        tgBotClient.sendUpdates(linkMapper.createLinkUpdate(
                linkId, link, "Получено обновление по ссылке " + link + "\n" + description, chatIds));
    }
}
