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

    CompletableFuture<List<LinkData>> getByLinkId(long linkId, long skip, long limit);

    CompletableFuture<Optional<LinkData>> getByChatIdLinkId(long chatId, long linkId);

    CompletableFuture<List<LinkData>> getByTagAndChatId(String tag, long chatId);

    CompletableFuture<Void> create(LinkData linkData);

    CompletableFuture<Void> deleteById(long id);

    CompletableFuture<Void> delete(LinkData link);
}
