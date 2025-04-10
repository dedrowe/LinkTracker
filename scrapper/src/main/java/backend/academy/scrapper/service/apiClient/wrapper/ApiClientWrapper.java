package backend.academy.scrapper.service.apiClient.wrapper;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;

public interface ApiClientWrapper {

    Optional<String> getLastUpdate(URI uri, LocalDateTime lastUpdate);

    void checkResource(URI uri);
}
