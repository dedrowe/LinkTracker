package backend.academy.scrapper.repository.tgchat;

import backend.academy.scrapper.entity.TgChat;
import java.util.List;
import java.util.Optional;

public interface TgChatRepository {

    Optional<TgChat> getById(long id);

    Optional<TgChat> getByChatId(long chatId);

    List<TgChat> getAllByIds(List<Long> ids);

    void create(TgChat tgChat);

    void deleteById(long id);

    void delete(TgChat tgChat);
}
