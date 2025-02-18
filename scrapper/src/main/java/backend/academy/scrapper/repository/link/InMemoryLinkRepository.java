package backend.academy.scrapper.repository.link;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.exceptionHandling.exceptions.ScrapperBaseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

@Repository
@Scope("singleton")
public class InMemoryLinkRepository implements LinkRepository {

    private final List<Link> links;

    private long idSequence;

    public InMemoryLinkRepository() {
        this(new ArrayList<>());
    }

    public InMemoryLinkRepository(List<Link> links) {
        this.links = links;
        idSequence = links.size() + 1L;
    }

    @Override
    public List<Link> getAll() {
        return List.copyOf(links);
    }

    @Override
    public Optional<Link> getById(long id) {
        return links.stream().filter(l -> l.id() == id).findFirst();
    }

    @Override
    public Optional<Link> getByLink(String link) {
        return links.stream().filter(l -> l.link().equals(link)).findFirst();
    }

    @Override
    public void create(Link link) {
        if (getByLink(link.link()).isPresent()) {
            throw new ScrapperBaseException("Эта ссылка уже существует");
        }
        link.id(idSequence++);
        links.add(link);
    }

    @Override
    public void update(Link newLink) {
        Optional<Link> curLink = getById(newLink.id());
        if (curLink.isEmpty()) {
            return;
        }
        Link link = curLink.orElseThrow();
        int index = links.indexOf(link);
        link.id(link.id());
        links.set(index, link);
    }

    @Override
    public void deleteById(int id) {
        Optional<Link> link = getById(id);
        link.ifPresent(links::remove);
    }

    @Override
    public void delete(Link link) {
        links.remove(link);
    }
}
