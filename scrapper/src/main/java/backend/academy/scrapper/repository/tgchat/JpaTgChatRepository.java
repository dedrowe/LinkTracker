package backend.academy.scrapper.repository.tgchat;

import backend.academy.scrapper.entity.TgChat;
import backend.academy.scrapper.exceptionHandling.exceptions.TgChatException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

@ConditionalOnProperty(havingValue = "ORM", prefix = "app", name = "access-type")
public interface JpaTgChatRepository extends TgChatRepository, Repository<TgChat, Long> {

    @Override
    @Async
    default CompletableFuture<Optional<TgChat>> getById(long id) {
        return CompletableFuture.completedFuture(getByIdSync(id));
    }

    @Override
    @Async
    default CompletableFuture<Optional<TgChat>> getByChatId(long chatId) {
        return CompletableFuture.completedFuture(getByChatIdSync(chatId));
    }

    @Override
    @Async
    @Transactional
    default CompletableFuture<Void> create(TgChat tgChat) {
        createSync(tgChat);
        return null;
    }

    @Override
    @Async
    @Transactional
    default CompletableFuture<Void> deleteById(long id) {
        deleteByIdSync(id);
        return null;
    }

    @Override
    @Async
    @Transactional
    default CompletableFuture<Void> delete(TgChat tgChat) {
        deleteSync(tgChat.chatId());
        return null;
    }

    @Query(value = "select t from TgChat t where t.id = :id and t.deleted = false")
    Optional<TgChat> getByIdSync(@Param("id") long id);

    @Query(value = "select t from TgChat t where t.chatId = :chatId and t.deleted = false")
    Optional<TgChat> getByChatIdSync(@Param("chatId") long chatId);

    @Query(value = "select t from TgChat t where t.chatId = :chatId")
    Optional<TgChat> getByChatIdWithDeletedSync(@Param("chatId") long chatId);

    @Transactional
    default void createSync(TgChat tgChat) {
        Optional<TgChat> data = getByChatIdWithDeletedSync(tgChat.chatId());

        if (data.isPresent()) {
            if (!data.orElseThrow().deleted()) {
                throw new TgChatException("Чат с таким id уже зарегистрирован", String.valueOf(tgChat.chatId()));
            }
            restoreTgChatSync(tgChat.chatId());
        } else {
            insertTgChatSync(tgChat.chatId());
        }
        TgChat newChat = getByChatIdSync(tgChat.chatId()).orElseThrow();
        tgChat.id(newChat.id());
    }

    @Modifying
    @Transactional
    @Query(value = "update TgChat t set t.deleted = false where t.chatId = :chatId")
    void restoreTgChatSync(@Param("chatId") long chatId);

    @Modifying
    @Transactional
    @Query(value = "insert into TgChat (chatId) values (:chatId)")
    void insertTgChatSync(@Param("chatId") long chatId);

    @Modifying
    @Transactional
    @Query(value = "update TgChat t set t.deleted = true where t.id = :id")
    void deleteByIdSync(@Param("id") long id);

    @Modifying
    @Transactional
    @Query(value = "update TgChat t set t.deleted = true where t.chatId = :chatId")
    void deleteSync(@Param("chatId") long chatId);
}
