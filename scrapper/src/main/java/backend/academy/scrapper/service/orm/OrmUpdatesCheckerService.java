package backend.academy.scrapper.service.orm;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.mapper.LinkMapper;
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.repository.linkdata.LinkDataRepository;
import backend.academy.scrapper.repository.tgchat.TgChatRepository;
import backend.academy.scrapper.service.LinkDispatcher;
import backend.academy.scrapper.service.apiClient.TgBotClient;
import backend.academy.scrapper.service.sql.SqlUpdatesCheckerService;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@ConditionalOnProperty(havingValue = "ORM", prefix = "app", name = "access-type")
public class OrmUpdatesCheckerService extends SqlUpdatesCheckerService {

    @Autowired
    public OrmUpdatesCheckerService(
            LinkDispatcher linkDispatcher,
            LinkDataRepository linkDataRepository,
            LinkRepository linkRepository,
            LinkMapper linkMapper,
            TgBotClient tgBotClient,
            TgChatRepository tgChatRepository,
            ScrapperConfig config) {
        super(linkDispatcher, linkDataRepository, linkRepository, linkMapper, tgBotClient, tgChatRepository, config);
    }

    public OrmUpdatesCheckerService(
            int batchSize,
            int threadPoolSize,
            LinkDispatcher linkDispatcher,
            LinkDataRepository linkDataRepository,
            LinkRepository linkRepository,
            LinkMapper linkMapper,
            TgBotClient tgBotClient,
            TgChatRepository tgChatRepository,
            Duration linksCheckInterval) {
        super(
                batchSize,
                threadPoolSize,
                linkDispatcher,
                linkDataRepository,
                linkRepository,
                linkMapper,
                tgBotClient,
                tgChatRepository,
                linksCheckInterval);
    }

    @Override
    @Transactional
    protected void sendUpdatesForLink(long linkId, String link, String description, List<LinkData> linkDataList) {
        List<Long> chatIds = linkDataList.stream().map(l -> l.tgChat().chatId()).toList();
        tgBotClient.sendUpdates(linkMapper.createLinkUpdate(
                linkId, link, "Получено обновление по ссылке " + link + "\n" + description, chatIds));
    }
}
