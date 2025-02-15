package backend.academy.scrapper.repository.link;

import backend.academy.scrapper.entity.Link;
import java.util.Optional;

public interface LinkRepository {

    Optional<Link> getById(long id);

    Optional<Link> getByLink(String link);

    void create(Link link);

    void update(Link link);

    void deleteById(int id);

    void delete(Link link);
}
