package backend.academy.scrapper.repository.linkdata;

import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.entity.jpa.JpaLinkData;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.hibernate.Hibernate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

@ConditionalOnProperty(havingValue = "ORM", prefix = "app", name = "access-type")
public interface JpaLinkDataRepository extends LinkDataRepository, CrudRepository<JpaLinkData, Long> {

    @Override
    @Async
    default CompletableFuture<List<JpaLinkData>> getAll() {
        return CompletableFuture.completedFuture(getAllSync());
    }

    @Override
    @Async
    default CompletableFuture<Optional<JpaLinkData>> getById(long id) {
        return CompletableFuture.completedFuture(getByIdSync(id));
    }

    @Override
    @Async
    @Transactional
    default CompletableFuture<List<JpaLinkData>> getByChatId(long chatId) {
        List<JpaLinkData> linksData = getByChatIdSync(chatId);
        linksData.forEach(this::fetchProperties);
        return CompletableFuture.completedFuture(linksData);
    }

    @Override
    @Async
    default CompletableFuture<List<JpaLinkData>> getByLinkId(long linkId) {
        return CompletableFuture.completedFuture(getByLinkIdSync(linkId));
    }

    @Override
    @Async
    default CompletableFuture<List<JpaLinkData>> getByLinkId(long linkId, long minId, long limit) {
        return CompletableFuture.completedFuture(getByLinkIdSync(linkId, minId, limit));
    }

    @Override
    @Async
    @Transactional
    default CompletableFuture<Optional<JpaLinkData>> getByChatIdLinkId(long chatId, long linkId) {
        Optional<JpaLinkData> linkData = getByChatIdLinkIdSync(chatId, linkId);
        return CompletableFuture.completedFuture(linkData);
    }

    @Override
    @Async
    @Transactional
    default CompletableFuture<List<JpaLinkData>> getByTagAndChatId(String tag, long chatId) {
        List<JpaLinkData> linksData = getByTagAndChatIdSync(tag, chatId);
        return CompletableFuture.completedFuture(linksData);
    }

    @Override
    @Async
    @Transactional
    default CompletableFuture<Void> create(LinkData linkData) {
        createSync(linkData);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    @Transactional
    default CompletableFuture<Void> deleteById(long id) {
        deleteByIdSync(id);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    @Transactional
    default CompletableFuture<Void> deleteLinkData(LinkData link) {
        deleteSync(link.chatId(), link.linkId());
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    @Transactional
    default CompletableFuture<Void> deleteByChatId(long chatId) {
        deleteByChatIdSync(chatId);
        return CompletableFuture.completedFuture(null);
    }

    @Query(value = "select l from JpaLinkData l where l.deleted = false")
    List<JpaLinkData> getAllSync();

    @Query(value = "select l from JpaLinkData l where l.id = :id and l.deleted = false")
    Optional<JpaLinkData> getByIdSync(@Param("id") long id);

    @Query(value = "select l from JpaLinkData l where l.chatId = :chatId and l.deleted = false")
    List<JpaLinkData> getByChatIdSync(@Param("chatId") long chatId);

    @Query(value = "select l from JpaLinkData l where l.linkId = :linkId and l.deleted = false")
    List<JpaLinkData> getByLinkIdSync(@Param("linkId") long linkId);

    @Query(
            value = "select * from links_data where link_id = :linkId and deleted = false and id > :minId limit :limit",
            nativeQuery = true)
    List<JpaLinkData> getByLinkIdSync(
            @Param("linkId") long linkId, @Param("minId") long minId, @Param("limit") long limit);

    @Query(value = "select l from JpaLinkData l where l.chatId = :chatId and l.linkId = :linkId and l.deleted = false")
    Optional<JpaLinkData> getByChatIdLinkIdSync(@Param("chatId") long chatId, @Param("linkId") long linkId);

    @Query(
            value = "select ld from JpaLinkData ld " + "join TgChat tc on ld.chatId = tc.id "
                    + "join LinkDataToTag ldt on ld.id = ldt.dataId "
                    + "join Tag t on ldt.tagId = t.id "
                    + "where t.tag = :tag and tc.chatId = :chatId and ld.deleted = false")
    List<JpaLinkData> getByTagAndChatIdSync(@Param("tag") String tag, @Param("chatId") long chatId);

    @Query(value = "select * from links_data where chat_id = :chatId and link_id = :linkId", nativeQuery = true)
    Optional<JpaLinkData> getByChatIdLinkIdWithDeletedSync(@Param("chatId") long chatId, @Param("linkId") long linkId);

    @Transactional
    default void createSync(LinkData linkData) {
        Optional<JpaLinkData> data = getByChatIdLinkIdWithDeletedSync(linkData.chatId(), linkData.linkId());
        data.ifPresent(l -> linkData.id(l.id()));
        save((JpaLinkData) linkData);
    }

    @Modifying
    @Transactional
    @Query(value = "update JpaLinkData ld set ld.deleted = true where ld.id = :id")
    void deleteByIdSync(@Param("id") long id);

    @Modifying
    @Transactional
    @Query(value = "update JpaLinkData ld set ld.deleted = true where ld.linkId = :linkId and ld.chatId = :chatId")
    void deleteSync(@Param("chatId") long chatId, @Param("linkId") long linkId);

    @Modifying
    @Transactional
    @Query(value = "update JpaLinkData ld set ld.deleted = true where ld.chatId = :chatId")
    void deleteByChatIdSync(@Param("chatId") long chatId);

    @Transactional
    default void fetchProperties(JpaLinkData linkData) {
        Hibernate.initialize(linkData.tags());
        Hibernate.initialize(linkData.filters());
    }
}
