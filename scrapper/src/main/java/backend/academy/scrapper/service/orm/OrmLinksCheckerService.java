package backend.academy.scrapper.service.orm;

import backend.academy.scrapper.dto.Update;
import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.Outbox;
import backend.academy.scrapper.entity.jpa.JpaLinkData;
import backend.academy.scrapper.repository.linkdata.JpaLinkDataRepository;
import backend.academy.scrapper.repository.outbox.OutboxRepository;
import backend.academy.scrapper.service.LinkDispatcher;
import backend.academy.scrapper.service.LinksCheckerService;
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

    private final JpaLinkDataRepository jpaLinkDataRepository;

    private final OutboxRepository outboxRepository;

    @Autowired
    public OrmLinksCheckerService(
            LinkDispatcher linkDispatcher,
            JpaLinkDataRepository jpaLinkDataRepository,
            OutboxRepository outboxRepository) {
        this.linkDispatcher = linkDispatcher;
        this.jpaLinkDataRepository = jpaLinkDataRepository;
        this.outboxRepository = outboxRepository;
    }

    @Override
    @Transactional
    public void setUpdatesForLink(Link link, List<Update> updates) {
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
                    outboxRepository.create(
                            new Outbox(link.id(), link.link(), linkData.tgChat().chatId(), update.description()));
                }
            }
        }
    }
}
