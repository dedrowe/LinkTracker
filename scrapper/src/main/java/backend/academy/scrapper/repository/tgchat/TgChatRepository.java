package backend.academy.scrapper.repository.tgchat;

import backend.academy.scrapper.entity.TgChat;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface TgChatRepository {

    CompletableFuture<Optional<TgChat>> getById(long id);

    CompletableFuture<Optional<TgChat>> getByChatId(long chatId);

    CompletableFuture<Void> create(TgChat tgChat);

    CompletableFuture<Void> deleteById(long id);

    CompletableFuture<Void> delete(TgChat tgChat);
}
