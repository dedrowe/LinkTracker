package backend.academy.scrapper.repository.linkdata;

import backend.academy.scrapper.entity.LinkData;
import backend.academy.shared.exceptions.BaseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Scope;
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
        this.data = data;
        idSequence = data.size() + 1L;
    }

    @Override
    public Optional<LinkData> getById(long id) {
        return data.stream().filter(l -> l.id() == id).findFirst();
    }

    @Override
    public List<LinkData> getAll() {
        return List.copyOf(data);
    }

    @Override
    public List<LinkData> getAll(int offset, int limit) {
        return List.copyOf(data.subList(offset, Math.min(data.size() - 1, offset + limit)));
    }

    @Override
    public List<LinkData> getByChatId(long chatId) {
        return List.copyOf(data.stream().filter(l -> l.chatId() == chatId).toList());
    }

    @Override
    public List<LinkData> getByLinkId(long linkId) {
        return List.copyOf(data.stream().filter(l -> l.linkId() == linkId).toList());
    }

    @Override
    public List<LinkData> getByChatId(long chatId, int offset, int limit) {
        List<LinkData> result = data.stream().filter(l -> l.chatId() == chatId).toList();
        result = result.subList(offset, Math.min(result.size(), offset + limit));
        return List.copyOf(result);
    }

    @Override
    public Optional<LinkData> getByChatIdLinkId(long chatId, long linkId) {
        return data.stream()
                .filter(l -> l.chatId() == chatId && l.linkId() == linkId)
                .findFirst();
    }

    @Override
    public void create(LinkData linkData) {
        Optional<LinkData> curLink = getByChatIdLinkId(linkData.chatId(), linkData.linkId());
        if (curLink.isPresent()) {
            throw new BaseException("Ссылка уже зарегистрирована");
        }
        linkData.id(idSequence++);
        data.add(linkData);
    }

    @Override
    public void update(LinkData link) {
        Optional<LinkData> curLink = getByChatIdLinkId(link.chatId(), link.linkId());
        if (curLink.isEmpty()) {
            return;
        }
        LinkData linkData = curLink.orElseThrow();
        int index = data.indexOf(linkData);
        link.id(linkData.id());
        data.set(index, link);
    }

    @Override
    public void deleteById(long id) {
        Optional<LinkData> link = getById(id);
        link.ifPresent(data::remove);
    }

    @Override
    public void delete(LinkData link) {
        data.remove(link);
    }
}
