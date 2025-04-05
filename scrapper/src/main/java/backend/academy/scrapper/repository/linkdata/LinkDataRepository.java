package backend.academy.scrapper.repository.linkdata;

import backend.academy.scrapper.entity.LinkData;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface LinkDataRepository {

    <T extends LinkData> CompletableFuture<List<T>> getAll();

    <T extends LinkData> CompletableFuture<Optional<T>> getById(long id);

    <T extends LinkData> CompletableFuture<List<T>> getByChatId(long chatId);

    <T extends LinkData> CompletableFuture<List<T>> getByLinkId(long linkId);

    <T extends LinkData> CompletableFuture<List<T>> getByLinkId(long linkId, long minId, long limit);

    <T extends LinkData> CompletableFuture<Optional<T>> getByChatIdLinkId(long chatId, long linkId);

    <T extends LinkData> CompletableFuture<List<T>> getByTagAndChatId(String tag, long chatId);

    CompletableFuture<Void> create(LinkData linkData);

    CompletableFuture<Void> deleteById(long id);

    CompletableFuture<Void> deleteLinkData(LinkData link);

    CompletableFuture<Void> deleteByChatId(long chatId);
}
