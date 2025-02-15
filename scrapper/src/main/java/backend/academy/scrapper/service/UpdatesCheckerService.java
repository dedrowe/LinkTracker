package backend.academy.scrapper.service;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.exceptionHandling.exceptions.NotFoundException;
import backend.academy.scrapper.exceptionHandling.exceptions.ScrapperBaseException;
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.repository.linkdata.LinkDataRepository;
import backend.academy.scrapper.service.apiClient.wrapper.ApiClientWrapper;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class UpdatesCheckerService {

    private final LinkDispatcher linkDispatcher;

    private final LinkDataRepository linkDataRepository;

    private final LinkRepository linkRepository;

    public UpdatesCheckerService(
            LinkDispatcher linkDispatcher, LinkDataRepository linkDataRepository, LinkRepository linkRepository) {
        this.linkDispatcher = linkDispatcher;
        this.linkDataRepository = linkDataRepository;
        this.linkRepository = linkRepository;
    }

    @Scheduled(fixedRate = 5000)
    public void checkUpdates() {
        List<LinkData> links = linkDataRepository.getAll();
        for (LinkData linkData : links) {
            Link link = linkRepository
                    .getById(linkData.linkId())
                    .orElseThrow(() -> new NotFoundException("Ссылка не найдена"));
            LocalDateTime lastCheck = linkData.lastCheck();
            if (!link.lastUpdate().isAfter(lastCheck)) {
                URI uri = URI.create(link.link());
                ApiClientWrapper client = linkDispatcher.dispatchLink(uri);

                link.lastUpdate(client.getLastUpdate(uri));
                linkRepository.update(link);

                linkData.lastCheck(LocalDateTime.now(ZoneOffset.UTC));
                linkDataRepository.update(linkData);
            }
            if (link.lastUpdate().isAfter(lastCheck)) {
                System.out.println("Update for link " + link.link());
            } else {
                System.out.println("No updates for link " + link.link());
            }
        }
    }

    public void checkResource(String url) {
        try {
            URI uri = new URI(url);
            ApiClientWrapper client = linkDispatcher.dispatchLink(uri);
            client.getLastUpdate(uri);
        } catch (URISyntaxException e) {
            throw new ScrapperBaseException("Ошибка в синтаксисе ссылки", e);
        }
    }
}
