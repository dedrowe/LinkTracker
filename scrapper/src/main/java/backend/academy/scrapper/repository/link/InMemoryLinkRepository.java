package backend.academy.scrapper.repository.link;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.exceptionHandling.exceptions.LinkException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@ConditionalOnProperty(havingValue = "RAM", prefix = "app", name = "access-type")
public class InMemoryLinkRepository implements LinkRepository {

    private final List<Link> links;

    private final AtomicLong idSequence;

    private final Duration linksCheckInterval;

    public InMemoryLinkRepository() {
        this(new ArrayList<>());
    }

    @Autowired
    public InMemoryLinkRepository(ScrapperConfig config) {
        this(new ArrayList<>(), Duration.ofSeconds(config.linksCheckIntervalSeconds()));
    }

    public InMemoryLinkRepository(List<Link> links) {
        this(links, Duration.ZERO);
    }

    public InMemoryLinkRepository(List<Link> links, Duration linksCheckInterval) {
        this.links = Collections.synchronizedList(links);
        idSequence = new AtomicLong(links.size() + 1L);
        this.linksCheckInterval = linksCheckInterval;
    }

    @Override
    @Async
    public CompletableFuture<List<Link>> getAll() {
        return CompletableFuture.completedFuture(List.copyOf(links));
    }

    @Override
    public CompletableFuture<List<Link>> getAll(long skip, long limit) {
        return CompletableFuture.completedFuture(
                List.copyOf(links.stream().skip(skip).limit(limit).toList()));
    }

    @Override
    public CompletableFuture<List<Link>> getAllNotChecked(long skip, long limit, LocalDateTime curTime) {
        return CompletableFuture.completedFuture(List.copyOf(links.stream()
                .filter(link -> curTime.isAfter(link.lastUpdate().plus(linksCheckInterval)))
                .skip(skip)
                .limit(limit)
                .toList()));
    }

    @Override
    @Async
    public CompletableFuture<Optional<Link>> getById(long id) {
        return CompletableFuture.completedFuture(getByIdInternal(id));
    }

    @Override
    @Async
    public CompletableFuture<Optional<Link>> getByLink(String link) {
        return CompletableFuture.completedFuture(getByLinkInternal(link));
    }

    @Override
    @Async
    public CompletableFuture<Void> create(Link link) {
        if (getByLinkInternal(link.link()).isPresent()) {
            throw new LinkException("Эта ссылка уже существует", link.link());
        }
        link.id(idSequence.incrementAndGet());
        links.add(link);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> update(Link newLink) {
        Optional<Link> curLink = getByIdInternal(newLink.id());
        if (curLink.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        Link link = curLink.orElseThrow();
        int index = links.indexOf(link);
        link.id(link.id());
        links.set(index, link);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> deleteById(long id) {
        Optional<Link> link = getByIdInternal(id);
        link.ifPresent(links::remove);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> delete(Link link) {
        links.remove(link);
        return CompletableFuture.completedFuture(null);
    }

    private Optional<Link> getByLinkInternal(String link) {
        return links.stream().filter(l -> l.link().equals(link)).findFirst();
    }

    private Optional<Link> getByIdInternal(long id) {
        return links.stream().filter(l -> l.id() == id).findFirst();
    }
}
