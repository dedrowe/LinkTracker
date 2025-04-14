package backend.academy.scrapper.repository.tgchat;

import backend.academy.scrapper.entity.TgChat;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@ConditionalOnProperty(havingValue = "ORM", prefix = "app", name = "access-type")
public interface JpaTgChatRepository extends TgChatRepository, JpaRepository<TgChat, Long> {

    @Override
    @Query(value = "select t from TgChat t where t.id = :id and t.deleted = false")
    Optional<TgChat> getById(@Param("id") long id);

    @Override
    @Query(value = "select t from TgChat t where t.chatId = :chatId and t.deleted = false")
    Optional<TgChat> getByChatId(long chatId);

    @Override
    @Query(value = "select t from TgChat t where t.id in :ids")
    List<TgChat> getAllByIds(@Param("ids") List<Long> ids);

    @Override
    @Transactional
    default void create(TgChat tgChat) {
        Optional<TgChat> data = getByChatIdWithDeleted(tgChat.chatId());
        data.ifPresent(chat -> tgChat.id(chat.id()));
        save(tgChat);
        flush();
    }

    @Override
    @Transactional
    default void update(TgChat tgChat) {
        update(tgChat.id(), tgChat.digest());
    }

    @Modifying
    @Transactional
    @Query(value = "update TgChat t set t.digest = :digest where t.id = :id and t.deleted = false")
    void update(@Param("id") long id, @Param("digest") LocalTime digest);

    @Query(value = "select t from TgChat t where t.chatId = :chatId")
    Optional<TgChat> getByChatIdWithDeleted(@Param("chatId") long chatId);

    @Modifying
    @Transactional
    @Query(value = "update TgChat t set t.deleted = false where t.chatId = :chatId")
    void restoreTgChat(@Param("chatId") long chatId);

    @Modifying
    @Transactional
    @Query(value = "insert into TgChat (chatId) values (:chatId)")
    void insertTgChat(@Param("chatId") long chatId);

    @Override
    @Modifying
    @Transactional
    @Query(value = "update TgChat t set t.deleted = true where t.id = :id")
    void deleteById(@Param("id") long id);

    @Override
    @Transactional
    default void delete(TgChat tgChat) {
        delete(tgChat.chatId());
    }

    @Modifying
    @Transactional
    @Query(value = "update TgChat t set t.deleted = true where t.chatId = :chatId")
    void delete(@Param("chatId") long chatId);
}
