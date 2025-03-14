package backend.academy.scrapper.repository.link;

import backend.academy.scrapper.entity.Link;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface LinkRepository {

    CompletableFuture<List<Link>> getAll();

    CompletableFuture<Optional<Link>> getById(long id);

    CompletableFuture<Optional<Link>> getByLink(String link);

    CompletableFuture<Void> create(Link link);

    CompletableFuture<Void> update(Link link);

    CompletableFuture<Void> deleteById(long id);

    CompletableFuture<Void> delete(Link link);
}
