package backend.academy.scrapper.service.orm;

import backend.academy.scrapper.dto.Update;
import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.jpa.JpaLinkData;
import backend.academy.scrapper.mapper.LinkMapper;
import backend.academy.scrapper.repository.linkdata.JpaLinkDataRepository;
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

    private final JpaLinkDataRepository jpaLinkDataRepository;

    @Autowired
    public OrmLinksCheckerService(
            LinkDispatcher linkDispatcher,
            LinkMapper linkMapper,
            TgBotClient tgBotClient,
            JpaLinkDataRepository jpaLinkDataRepository) {
        this.linkDispatcher = linkDispatcher;
        this.linkMapper = linkMapper;
        this.tgBotClient = tgBotClient;
        this.jpaLinkDataRepository = jpaLinkDataRepository;
    }

    @Override
    @Transactional
    public void sendUpdatesForLink(Link link, List<Update> updates) {
        List<JpaLinkData> linksData = jpaLinkDataRepository.fetchFilters(
                link.linksData().stream().map(JpaLinkData::id).toList());
        for (JpaLinkData linkData : linksData) {
            boolean skip;
            for (Update update : updates) {
                skip = false;
                for (Filter filter : linkData.filters()) {
                    String[] tokens = filter.filter().split(":");
                    if (update.filters().containsKey(tokens[0])
                            && update.filters().get(tokens[0]).equals(tokens[1])) {
                        skip = true;
                        break;
                    }
                }
                if (!skip) {
                    tgBotClient.sendUpdates(linkMapper.createLinkUpdate(
                            link.id(),
                            link.link(),
                            "Получено обновление по ссылке " + link.link() + "\n" + update.description(),
                            List.of(linkData.tgChat().chatId())));
                }
            }
        }
    }
}
