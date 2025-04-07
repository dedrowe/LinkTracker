package backend.academy.scrapper.service.orm;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.mapper.LinkMapper;
import backend.academy.scrapper.service.LinkDispatcher;
import backend.academy.scrapper.service.LinksCheckerService;
import backend.academy.scrapper.service.apiClient.TgBotClient;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@ConditionalOnProperty(havingValue = "ORM", prefix = "app", name = "access-type")
public class OrmLinksCheckerService extends LinksCheckerService {

    private final TgBotClient tgBotClient;

    private final LinkMapper linkMapper;

    @Autowired
    public OrmLinksCheckerService(LinkDispatcher linkDispatcher, LinkMapper linkMapper, TgBotClient tgBotClient) {
        this.linkDispatcher = linkDispatcher;
        this.linkMapper = linkMapper;
        this.tgBotClient = tgBotClient;
    }

    @Override
    @Transactional
    public void sendUpdatesForLink(Link link, String description) {
        List<Long> chatIds =
                link.linksData().stream().map(l -> l.tgChat().chatId()).toList();
        if (chatIds.isEmpty()) {
            return;
        }
        tgBotClient.sendUpdates(linkMapper.createLinkUpdate(
                link.id(), link.link(), "Получено обновление по ссылке " + link.link() + "\n" + description, chatIds));
    }
}
