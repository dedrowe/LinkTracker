package backend.academy.scrapper.service.apiClient.wrapper;

import backend.academy.scrapper.dto.Update;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

public interface ApiClientWrapper {

    List<Update> getLastUpdate(URI uri, LocalDateTime lastUpdate);

    void checkResource(URI uri);
}
