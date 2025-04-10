package backend.academy.scrapper.service.sql;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.entity.TgChat;
import backend.academy.scrapper.mapper.LinkMapper;
import backend.academy.scrapper.repository.linkdata.LinkDataRepository;
import backend.academy.scrapper.repository.tgchat.TgChatRepository;
import backend.academy.scrapper.service.LinkDispatcher;
import backend.academy.scrapper.service.LinksCheckerService;
import backend.academy.scrapper.service.apiClient.TgBotClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    private final int batchSize;

    private final LinkDataRepository linkDataRepository;

    private final LinkMapper linkMapper;

    private final TgBotClient tgBotClient;

    private final TgChatRepository tgChatRepository;

    // Здесь не получится использовать lombok из-за наследуемого поля linkDispatcher
    public SqlLinksCheckerService(
            LinkDispatcher linkDispatcher,
            int batchSize,
            LinkDataRepository linkDataRepository,
            LinkMapper linkMapper,
            TgBotClient tgBotClient,
            TgChatRepository tgChatRepository) {
        this.linkDispatcher = linkDispatcher;
        this.batchSize = batchSize;
        this.linkDataRepository = linkDataRepository;
        this.linkMapper = linkMapper;
        this.tgBotClient = tgBotClient;
        this.tgChatRepository = tgChatRepository;
    }

    @Autowired
    public SqlLinksCheckerService(
            LinkDispatcher linkDispatcher,
            LinkDataRepository linkDataRepository,
            LinkMapper linkMapper,
            TgBotClient tgBotClient,
            TgChatRepository tgChatRepository,
            ScrapperConfig config) {
        this.linkDispatcher = linkDispatcher;
        this.linkDataRepository = linkDataRepository;
        this.linkMapper = linkMapper;
        this.tgBotClient = tgBotClient;
        this.tgChatRepository = tgChatRepository;
        batchSize = config.updatesChecker().batchSize();
    }

    @Override
    @Transactional
    public void sendUpdatesForLink(Link link, String description) {
        long minId = -1;
        while (true) {
            List<LinkData> linkDataList = linkDataRepository.getByLinkId(link.id(), minId, batchSize);
            if (linkDataList.isEmpty()) {
                break;
            }
            sendUpdatesForLink(link.id(), link.link(), description, linkDataList);
            minId = linkDataList.getLast().id();
        }
    }

    private void sendUpdatesForLink(long linkId, String link, String description, List<LinkData> linkDataList) {
        List<Long> chatIds = new ArrayList<>();
        List<Optional<TgChat>> chats = linkDataList.stream()
                .map(linkData -> tgChatRepository.getById(linkData.chatId()))
                .toList();
        for (int i = 0; i < chats.size(); i++) {
            int finalI = i;
            chats.get(i).ifPresentOrElse(tgChat -> chatIds.add(tgChat.chatId()), () -> {
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
