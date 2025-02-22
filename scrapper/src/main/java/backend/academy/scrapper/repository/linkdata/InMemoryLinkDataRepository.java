package backend.academy.scrapper.repository.linkdata;

import backend.academy.scrapper.entity.LinkData;
import backend.academy.shared.exceptions.BaseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

@Repository
@Scope("singleton")
public class InMemoryLinkDataRepository implements LinkDataRepository {

    private final List<LinkData> data;

    private long idSequence;

    public InMemoryLinkDataRepository() {
        this(new ArrayList<>());
    }

    public InMemoryLinkDataRepository(List<LinkData> data) {
        this.data = Collections.synchronizedList(data);
        idSequence = data.size() + 1L;
    }

    @Override
    @Async
    public Future<List<LinkData>> getAll() {
        return CompletableFuture.completedFuture(List.copyOf(data));
    }

    @Override
    @Async
    public Future<Optional<LinkData>> getById(long id) {
        return CompletableFuture.completedFuture(getByIdInternal(id));
    }

    @Override
    @Async
    public Future<List<LinkData>> getByChatId(long chatId) {
        return CompletableFuture.completedFuture(getByChatIdInternal(chatId));
    }

    @Override
    @Async
    public Future<List<LinkData>> getByLinkId(long linkId) {
        return CompletableFuture.completedFuture(getByLinkIdInternal(linkId));
    }

    @Override
    @Async
    public Future<Optional<LinkData>> getByChatIdLinkId(long chatId, long linkId) {
        return CompletableFuture.completedFuture(getByChatIdLinkIdInternal(chatId, linkId));
    }

    @Override
    @Async
    public Future<Void> create(LinkData linkData) {
        Optional<LinkData> curLink = getByChatIdLinkIdInternal(linkData.chatId(), linkData.linkId());
        if (curLink.isPresent()) {
            throw new BaseException("Ссылка уже зарегистрирована");
        }
        linkData.id(idSequence++);
        data.add(linkData);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public Future<Void> update(LinkData link) {
        Optional<LinkData> curLink = getByChatIdLinkIdInternal(link.chatId(), link.linkId());
        if (curLink.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        LinkData linkData = curLink.orElseThrow();
        int index = data.indexOf(linkData);
        link.id(linkData.id());
        data.set(index, link);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public Future<Void> deleteById(long id) {
        Optional<LinkData> link = getByIdInternal(id);
        link.ifPresent(data::remove);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public Future<Void> delete(LinkData link) {
        data.remove(link);
        return CompletableFuture.completedFuture(null);
    }

    private Optional<LinkData> getByIdInternal(long id) {
        return data.stream().filter(l -> l.id() == id).findFirst();
    }

    private List<LinkData> getByChatIdInternal(long chatId) {
        return List.copyOf(data.stream().filter(l -> l.chatId() == chatId).toList());
    }

    private List<LinkData> getByLinkIdInternal(long linkId) {
        return List.copyOf(data.stream().filter(l -> l.linkId() == linkId).toList());
    }

    private Optional<LinkData> getByChatIdLinkIdInternal(long chatId, long linkId) {
        return data.stream()
                .filter(l -> l.chatId() == chatId && l.linkId() == linkId)
                .findFirst();
    }
}
