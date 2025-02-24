package backend.academy.scrapper.service;

import backend.academy.scrapper.exceptionHandling.exceptions.WrongServiceException;
import backend.academy.scrapper.service.apiClient.wrapper.ApiClientWrapper;
import java.net.URI;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class LinkDispatcher {

    private final Map<String, ApiClientWrapper> apiClients;

    @SuppressWarnings("PMD.UnusedLocalVariable")
    public ApiClientWrapper dispatchLink(URI uri) {
        ApiClientWrapper apiClient = apiClients.getOrDefault(uri.getAuthority(), null);
        if (apiClient == null) {
            throw new WrongServiceException("Отслеживание ссылок этого сервиса не поддерживается", uri.toString());
        }
        return apiClient;
    }
}
