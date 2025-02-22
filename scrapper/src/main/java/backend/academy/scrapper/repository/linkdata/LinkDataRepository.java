package backend.academy.scrapper.repository.linkdata;

import backend.academy.scrapper.entity.LinkData;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;

public interface LinkDataRepository {

    Future<List<LinkData>> getAll();

    Future<Optional<LinkData>> getById(long id);

    Future<List<LinkData>> getByChatId(long chatId);

    Future<List<LinkData>> getByLinkId(long linkId);

    Future<Optional<LinkData>> getByChatIdLinkId(long chatId, long linkId);

    Future<Void> create(LinkData linkData);

    Future<Void> update(LinkData link);

    Future<Void> deleteById(long id);

    Future<Void> delete(LinkData link);
}
