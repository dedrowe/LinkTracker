package backend.academy.scrapper.service.sql;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.dto.Update;
import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.entity.TgChat;
import backend.academy.scrapper.mapper.LinkMapper;
import backend.academy.scrapper.repository.linkdata.LinkDataRepository;
import backend.academy.scrapper.repository.tgchat.TgChatRepository;
import backend.academy.scrapper.service.FiltersService;
import backend.academy.scrapper.service.LinkDispatcher;
import backend.academy.scrapper.service.LinksCheckerService;
import backend.academy.scrapper.service.apiClient.TgBotClient;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
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

    private final FiltersService filtersService;

    // Здесь не получится использовать lombok из-за наследуемого поля linkDispatcher
    public SqlLinksCheckerService(
            LinkDispatcher linkDispatcher,
            int batchSize,
            LinkDataRepository linkDataRepository,
            LinkMapper linkMapper,
            TgBotClient tgBotClient,
            TgChatRepository tgChatRepository,
            FiltersService filtersService) {
        this.linkDispatcher = linkDispatcher;
        this.batchSize = batchSize;
        this.linkDataRepository = linkDataRepository;
        this.linkMapper = linkMapper;
        this.tgBotClient = tgBotClient;
        this.tgChatRepository = tgChatRepository;
        this.filtersService = filtersService;
    }

    @Autowired
    public SqlLinksCheckerService(
            LinkDispatcher linkDispatcher,
            LinkDataRepository linkDataRepository,
            LinkMapper linkMapper,
            TgBotClient tgBotClient,
            TgChatRepository tgChatRepository,
            ScrapperConfig config,
            FiltersService filtersService) {
        this.linkDispatcher = linkDispatcher;
        this.linkDataRepository = linkDataRepository;
        this.linkMapper = linkMapper;
        this.tgBotClient = tgBotClient;
        this.tgChatRepository = tgChatRepository;
        batchSize = config.updatesChecker().batchSize();
        this.filtersService = filtersService;
    }

    @Override
    @Transactional
    public void sendUpdatesForLink(Link link, List<Update> updates) {
        long minId = -1;
        while (true) {
            List<LinkData> linkDataList = linkDataRepository.getByLinkId(link.id(), minId, batchSize);
            if (linkDataList.isEmpty()) {
                break;
            }

            List<Long> ids = linkDataList.stream().map(LinkData::id).toList();

            List<Filter> filters = filtersService.getAllByDataIds(ids);
            Map<Long, List<Filter>> filtersMap = filters.stream().collect(Collectors.groupingBy(Filter::dataId));

            List<TgChat> chats = tgChatRepository.getAllByIds(ids);
            Map<Long, TgChat> chatsMap = chats.stream().collect(Collectors.toMap(TgChat::id, Function.identity()));

            for (LinkData linkData : linkDataList) {
                boolean skip;
                for (Update update : updates) {
                    skip = false;
                    for (Filter filter : filtersMap.getOrDefault(linkData.id(), List.of())) {
                        String[] tokens = filter.filter().split(":");
                        if (update.filters().containsKey(tokens[0])
                                && update.filters().get(tokens[0]).equals(tokens[1])) {
                            skip = true;
                            break;
                        }
                    }
                    if (!skip) {
                        sendUpdatesForLink(link.id(), link.link(), update.description(), linkData, chatsMap);
                    }
                }
            }
            minId = linkDataList.getLast().id();
        }
    }

    private void sendUpdatesForLink(
            long linkId, String link, String description, LinkData linkData, Map<Long, TgChat> chatsMap) {
        TgChat chat = chatsMap.getOrDefault(linkData.chatId(), null);

        if (chat == null) {
            try (var ignored = MDC.putCloseable("id", String.valueOf(linkData.chatId()))) {
                log.warn("Чат не найден");
            }
            return;
        }

        tgBotClient.sendUpdates(linkMapper.createLinkUpdate(
                linkId, link, "Получено обновление по ссылке " + link + "\n" + description, List.of(chat.chatId())));
    }
}
