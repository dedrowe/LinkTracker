package backend.academy.scrapper.repository.link;

import backend.academy.scrapper.entity.Link;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;

public interface LinkRepository {

    Future<List<Link>> getAll();

    Future<Optional<Link>> getById(long id);

    Future<Optional<Link>> getByLink(String link);

    Future<Void> create(Link link);

    Future<Void> update(Link link);

    Future<Void> deleteById(int id);

    Future<Void> delete(Link link);
}
