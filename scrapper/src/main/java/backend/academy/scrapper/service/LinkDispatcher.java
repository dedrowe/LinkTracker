package backend.academy.scrapper.service;

import backend.academy.scrapper.service.apiClient.wrapper.ApiClientWrapper;
import backend.academy.shared.exceptions.BaseException;
import java.net.URI;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LinkDispatcher {

    private final Map<String, ApiClientWrapper> apiClients;

    @Autowired
    public LinkDispatcher(Map<String, ApiClientWrapper> apiClients) {
        this.apiClients = apiClients;
    }

    public ApiClientWrapper dispatchLink(URI uri) {
        ApiClientWrapper apiClient = apiClients.getOrDefault(uri.getAuthority(), null);
        if (apiClient == null) {
            String exceptionMessage = "Отслеживание ссылок этого сервиса не поддерживается";
            BaseException ex = new BaseException(exceptionMessage);
            MDC.put("uri", uri.toString());
            log.warn(exceptionMessage, ex);
            MDC.clear();
            throw ex;
        }
        return apiClient;
    }
}
