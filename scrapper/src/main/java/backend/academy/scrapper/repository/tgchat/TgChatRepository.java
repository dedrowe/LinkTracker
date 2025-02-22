package backend.academy.scrapper.repository.tgchat;

import backend.academy.scrapper.entity.TgChat;
import java.util.Optional;
import java.util.concurrent.Future;

public interface TgChatRepository {

    Future<Optional<TgChat>> getById(long id);

    Future<Optional<TgChat>> getByChatId(long chatId);

    Future<Void> create(TgChat tgChat);

    Future<Void> deleteById(long id);

    Future<Void> delete(TgChat tgChat);
}
