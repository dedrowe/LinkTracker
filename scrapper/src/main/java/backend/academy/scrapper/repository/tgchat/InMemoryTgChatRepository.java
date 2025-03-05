package backend.academy.scrapper.repository.tgchat;

import backend.academy.scrapper.entity.TgChat;
import backend.academy.scrapper.exceptionHandling.exceptions.TgChatException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

@Repository
@Scope
@Slf4j
public class InMemoryTgChatRepository implements TgChatRepository {

    private final List<TgChat> data;

    private final AtomicLong idSequence;

    public InMemoryTgChatRepository() {
        this(new ArrayList<>());
    }

    public InMemoryTgChatRepository(List<TgChat> data) {
        this.data = Collections.synchronizedList(data);
        idSequence = new AtomicLong(data.size() + 1L);
    }

    @Override
    @Async
    public CompletableFuture<Optional<TgChat>> getById(long id) {
        return CompletableFuture.completedFuture(getByIdInternal(id));
    }

    @Override
    @Async
    public CompletableFuture<Optional<TgChat>> getByChatId(long chatId) {
        return CompletableFuture.completedFuture(getByChatIdInternal(chatId));
    }

    @Override
    @Async
    public CompletableFuture<Void> create(TgChat tgChat) {
        Optional<TgChat> curChat = getByChatIdInternal(tgChat.chatId());
        if (curChat.isPresent()) {
            throw new TgChatException("Чат с таким id уже зарегистрирован", String.valueOf(tgChat.chatId()));
        }
        tgChat.id(idSequence.incrementAndGet());
        data.add(tgChat);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> deleteById(long id) {
        Optional<TgChat> chat = getByIdInternal(id);
        chat.ifPresent(data::remove);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> delete(TgChat tgChat) {
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
