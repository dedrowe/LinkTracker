package backend.academy.scrapper.repository.linkdata;

import backend.academy.scrapper.entity.LinkData;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface LinkDataRepository {

    CompletableFuture<List<LinkData>> getAll();

    CompletableFuture<Optional<LinkData>> getById(long id);

    CompletableFuture<List<LinkData>> getByChatId(long chatId);

    CompletableFuture<List<LinkData>> getByLinkId(long linkId);

    CompletableFuture<Optional<LinkData>> getByChatIdLinkId(long chatId, long linkId);

    CompletableFuture<Void> create(LinkData linkData);

    CompletableFuture<Void> update(LinkData link);

    CompletableFuture<Void> deleteById(long id);

    CompletableFuture<Void> delete(LinkData link);
}
