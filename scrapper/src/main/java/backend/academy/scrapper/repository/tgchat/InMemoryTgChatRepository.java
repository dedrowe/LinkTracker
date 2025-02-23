package backend.academy.scrapper.repository.tgchat;

import backend.academy.scrapper.entity.TgChat;
import backend.academy.shared.exceptions.BaseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

@Repository
@Scope("singleton")
@Slf4j
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
    @Async
    public Future<Optional<TgChat>> getById(long id) {
        return CompletableFuture.completedFuture(getByIdInternal(id));
    }

    @Override
    @Async
    public Future<Optional<TgChat>> getByChatId(long chatId) {
        return CompletableFuture.completedFuture(getByChatIdInternal(chatId));
    }

    @Override
    @Async
    @SuppressWarnings("PMD.UnusedLocalVariable")
    public Future<Void> create(TgChat tgChat) {
        Optional<TgChat> curChat = getByChatIdInternal(tgChat.chatId());
        if (curChat.isPresent()) {
            String exceptionMessage = "Чат с таким id уже зарегистрирован";
            BaseException ex = new BaseException(exceptionMessage);
            try (var var = MDC.putCloseable("id", String.valueOf(tgChat.chatId()))) {
                log.error(exceptionMessage, ex);
            }
            throw ex;
        }
        tgChat.id(idSequence++);
        data.add(tgChat);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public Future<Void> deleteById(long id) {
        Optional<TgChat> chat = getByIdInternal(id);
        chat.ifPresent(data::remove);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public Future<Void> delete(TgChat tgChat) {
        data.remove(tgChat);
        return CompletableFuture.completedFuture(null);
    }

    private Optional<TgChat> getByIdInternal(long id) {
        return data.stream().filter(chat -> chat.id() == id).findFirst();
    }

    private Optional<TgChat> getByChatIdInternal(long chatId) {
        return data.stream().filter(chat -> chat.chatId() == chatId).findFirst();
    }
}
