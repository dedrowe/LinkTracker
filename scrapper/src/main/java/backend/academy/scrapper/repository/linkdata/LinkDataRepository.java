package backend.academy.scrapper.repository.linkdata;

import backend.academy.scrapper.entity.LinkData;
import java.util.List;
import java.util.Optional;

public interface LinkDataRepository {

    Optional<LinkData> getById(long id);

    List<LinkData> getAll();

    List<LinkData> getAll(int offset, int limit);

    List<LinkData> getByChatId(long chatId);

    List<LinkData> getByChatId(long chatId, int offset, int limit);

    Optional<LinkData> getByChatIdLinkId(long chatId, long linkId);

    void create(LinkData linkData);

    void update(LinkData link);

    void deleteById(long id);

    void delete(LinkData link);
}
