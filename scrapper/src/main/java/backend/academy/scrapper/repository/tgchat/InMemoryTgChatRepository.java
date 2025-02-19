package backend.academy.scrapper.repository.tgchat;

import backend.academy.scrapper.entity.TgChat;
import backend.academy.shared.exceptions.BaseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

@Repository
@Scope("singleton")
public class InMemoryTgChatRepository implements TgChatRepository {

    private final List<TgChat> data;

    private long idSequence;

    public InMemoryTgChatRepository() {
        this(new ArrayList<>());
    }

    public InMemoryTgChatRepository(List<TgChat> data) {
        this.data = data;
        idSequence = data.size() + 1L;
    }

    @Override
    public Optional<TgChat> getById(long id) {
        return data.stream().filter(chat -> chat.id() == id).findFirst();
    }

    @Override
    public Optional<TgChat> getByChatId(long chatId) {
        return data.stream().filter(chat -> chat.chatId() == chatId).findFirst();
    }

    @Override
    public void create(TgChat tgChat) {
        Optional<TgChat> curChat = getByChatId(tgChat.chatId());
        if (curChat.isPresent()) {
            throw new BaseException("Чат с таким id уже зарегистрирован");
        }
        tgChat.id(idSequence++);
        data.add(tgChat);
    }

    @Override
    public void deleteById(long id) {
        Optional<TgChat> chat = getById(id);
        chat.ifPresent(data::remove);
    }

    @Override
    public void delete(TgChat tgChat) {
        data.remove(tgChat);
    }
}
