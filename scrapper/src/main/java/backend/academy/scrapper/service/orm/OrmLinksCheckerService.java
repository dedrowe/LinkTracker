package backend.academy.scrapper.service.orm;

import static backend.academy.scrapper.utils.FutureUnwrapper.unwrap;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.mapper.LinkMapper;
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.service.LinkDispatcher;
import backend.academy.scrapper.service.LinksCheckerService;
import backend.academy.scrapper.service.apiClient.TgBotClient;
import backend.academy.scrapper.service.apiClient.wrapper.ApiClientWrapper;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
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

    private final LinkRepository linkRepository;

    @Autowired
    public OrmLinksCheckerService(
            LinkDispatcher linkDispatcher,
            LinkMapper linkMapper,
            TgBotClient tgBotClient,
            LinkRepository linkRepository) {
        this.linkDispatcher = linkDispatcher;
        this.linkMapper = linkMapper;
        this.tgBotClient = tgBotClient;
        this.linkRepository = linkRepository;
    }

    @Override
    @Transactional
    public void checkUpdatesForLink(Link link) {
        URI uri = URI.create(link.link());
        ApiClientWrapper client = linkDispatcher.dispatchLink(uri);

        Optional<String> description = client.getLastUpdate(uri, link.lastUpdate());

        if (description.isPresent()) {
            sendUpdatesForLink(link.id(), link.link(), description.orElseThrow(), link.linksData());
        }
        link.lastUpdate(LocalDateTime.now(ZoneOffset.UTC));
        unwrap(linkRepository.update(link));
    }

    private void sendUpdatesForLink(long linkId, String link, String description, List<LinkData> linkDataList) {
        List<Long> chatIds = linkDataList.stream().map(l -> l.tgChat().chatId()).toList();
        if (chatIds.isEmpty()) {
            return;
        }
        tgBotClient.sendUpdates(linkMapper.createLinkUpdate(
                linkId, link, "Получено обновление по ссылке " + link + "\n" + description, chatIds));
    }
}
