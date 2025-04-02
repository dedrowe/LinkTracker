package backend.academy.scrapper.repository.link;

import backend.academy.scrapper.entity.Link;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface LinkRepository {

    CompletableFuture<List<Link>> getAll();

    CompletableFuture<List<Link>> getAll(long skip, long limit);

    CompletableFuture<List<Link>> getAllNotChecked(long limit, LocalDateTime curTime, long checkInterval);

    CompletableFuture<Optional<Link>> getById(long id);

    CompletableFuture<Optional<Link>> getByLink(String link);

    CompletableFuture<Void> create(Link link);

    CompletableFuture<Void> update(Link link);

    CompletableFuture<Void> deleteById(long id);

    CompletableFuture<Void> deleteLink(Link link);
}
