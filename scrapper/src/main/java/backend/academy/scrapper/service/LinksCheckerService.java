package backend.academy.scrapper.service;

import backend.academy.scrapper.dto.Update;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.exceptionHandling.exceptions.WrongServiceException;
import backend.academy.scrapper.service.apiClient.wrapper.ApiClientWrapper;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

public abstract class LinksCheckerService {

    protected LinkDispatcher linkDispatcher;

    @Transactional
    public abstract void sendUpdatesForLink(Link link, List<Update> updates);

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

    public List<Update> getLinkUpdate(Link link) {
        URI uri = URI.create(link.link());
        ApiClientWrapper client = linkDispatcher.dispatchLink(uri);
        return client.getLastUpdate(uri, link.lastUpdate());
    }
}
