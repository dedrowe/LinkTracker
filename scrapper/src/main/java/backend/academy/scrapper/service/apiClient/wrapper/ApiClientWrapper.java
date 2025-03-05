package backend.academy.scrapper.service.apiClient.wrapper;

import java.net.URI;
import java.time.LocalDateTime;

public interface ApiClientWrapper {

    LocalDateTime getLastUpdate(URI uri);
}
