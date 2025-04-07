package backend.academy.scrapper.repository.link;

import backend.academy.scrapper.entity.Link;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LinkRepository {

    List<Link> getAll();

    List<Link> getNotChecked(long limit, LocalDateTime curTime, long checkInterval);

    Optional<Link> getById(long id);

    Optional<Link> getByLink(String link);

    void create(Link link);

    void update(Link link);

    void deleteById(long id);

    void deleteLink(Link link);
}
