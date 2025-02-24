package backend.academy.scrapper.repository.link;

import backend.academy.scrapper.entity.Link;
import backend.academy.shared.exceptions.BaseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

@Repository
@Scope
@Slf4j
public class InMemoryLinkRepository implements LinkRepository {

    private final List<Link> links;

    private long idSequence;

    public InMemoryLinkRepository() {
        this(new ArrayList<>());
    }

    public InMemoryLinkRepository(List<Link> links) {
        this.links = Collections.synchronizedList(links);
        idSequence = links.size() + 1L;
    }

    @Override
    @Async
    public Future<List<Link>> getAll() {
        return CompletableFuture.completedFuture(List.copyOf(links));
    }

    @Override
    @Async
    public Future<Optional<Link>> getById(long id) {
        return CompletableFuture.completedFuture(getByIdInternal(id));
    }

    @Override
    @Async
    public Future<Optional<Link>> getByLink(String link) {
        return CompletableFuture.completedFuture(getByLinkInternal(link));
    }

    @Override
    @Async
    @SuppressWarnings("PMD.UnusedLocalVariable")
    public Future<Void> create(Link link) {
        if (getByLinkInternal(link.link()).isPresent()) {
            String exceptionMessage = "Эта ссылка уже существует";
            BaseException ex = new BaseException(exceptionMessage);
            try (var var = MDC.putCloseable("link", link.link())) {
                log.error(exceptionMessage, ex);
            }
            throw ex;
        }
        link.id(idSequence++);
        links.add(link);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public Future<Void> update(Link newLink) {
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
    public Future<Void> deleteById(int id) {
        Optional<Link> link = getByIdInternal(id);
        link.ifPresent(links::remove);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public Future<Void> delete(Link link) {
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
