package backend.academy.scrapper.service;

import backend.academy.scrapper.exceptionHandling.exceptions.ScrapperBaseException;
import backend.academy.scrapper.service.apiClient.wrapper.ApiClientWrapper;
import java.net.URI;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LinkDispatcher {

    private final Map<String, ApiClientWrapper> apiClients;

    @Autowired
    public LinkDispatcher(Map<String, ApiClientWrapper> apiClients) {
        this.apiClients = apiClients;
    }

    public ApiClientWrapper dispatchLink(URI uri) {
        ApiClientWrapper apiClient = apiClients.getOrDefault(uri.getAuthority(), null);
        if (apiClient == null) {
            throw new ScrapperBaseException("Отслеживание ссылок этого сервиса не поддерживается");
        }
        return apiClient;
    }
}
