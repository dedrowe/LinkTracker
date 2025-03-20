package backend.academy.scrapper.repository.linkdata;

import backend.academy.scrapper.entity.LinkData;
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
public interface JpaLinkDataRepository extends LinkDataRepository, CrudRepository<LinkData, Long> {

    @Override
    @Async
    default CompletableFuture<List<LinkData>> getAll() {
        return CompletableFuture.completedFuture(getAllSync());
    }

    @Override
    @Async
    default CompletableFuture<Optional<LinkData>> getById(long id) {
        return CompletableFuture.completedFuture(getByIdSync(id));
    }

    @Override
    @Async
    @Transactional
    default CompletableFuture<List<LinkData>> getByChatId(long chatId) {
        List<LinkData> linksData = getByChatIdSync(chatId);
        linksData.forEach(linkData -> {
            Hibernate.initialize(linkData.tags());
            Hibernate.initialize(linkData.filters());
        });
        return CompletableFuture.completedFuture(linksData);
    }

    @Override
    @Async
    default CompletableFuture<List<LinkData>> getByLinkId(long linkId) {
        return CompletableFuture.completedFuture(getByLinkIdSync(linkId));
    }

    @Override
    @Async
    default CompletableFuture<List<LinkData>> getByLinkId(long linkId, long skip, long limit) {
        return CompletableFuture.completedFuture(getByLinkIdSync(linkId, skip, limit));
    }

    @Override
    @Async
    @Transactional
    default CompletableFuture<Optional<LinkData>> getByChatIdLinkId(long chatId, long linkId) {
        Optional<LinkData> linkData = getByChatIdLinkIdSync(chatId, linkId);
        if (linkData.isPresent()) {
            Hibernate.initialize(linkData.orElseThrow().tags());
            Hibernate.initialize(linkData.orElseThrow().filters());
        }
        return CompletableFuture.completedFuture(linkData);
    }

    @Override
    @Async
    @Transactional
    default CompletableFuture<List<LinkData>> getByTagAndChatId(String tag, long chatId) {
        List<LinkData> linksData = getByTagAndChatIdSync(tag, chatId);
        linksData.forEach(linkData -> {
            Hibernate.initialize(linkData.tags());
            Hibernate.initialize(linkData.filters());
        });
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

    @Query(value = "select l from LinkData l where l.deleted = false")
    List<LinkData> getAllSync();

    @Query(value = "select l from LinkData l where l.id = :id and l.deleted = false")
    Optional<LinkData> getByIdSync(@Param("id") long id);

    @Query(value = "select l from LinkData l where l.chatId = :chatId and l.deleted = false")
    List<LinkData> getByChatIdSync(@Param("chatId") long chatId);

    @Query(value = "select l from LinkData l where l.linkId = :linkId and l.deleted = false")
    List<LinkData> getByLinkIdSync(@Param("linkId") long linkId);

    @Query(
            value =
                    "select * from links_data where link_id = :linkId and deleted = false offset :skip limit :limit for update",
            nativeQuery = true)
    List<LinkData> getByLinkIdSync(@Param("linkId") long linkId, @Param("skip") long skip, @Param("limit") long limit);

    @Query(value = "select l from LinkData l where l.chatId = :chatId and l.linkId = :linkId and l.deleted = false")
    Optional<LinkData> getByChatIdLinkIdSync(@Param("chatId") long chatId, @Param("linkId") long linkId);

    @Query(
            value = "select ld from LinkData ld " + "join TgChat tc on ld.chatId = tc.id "
                    + "join LinkDataToTag ldt on ld.id = ldt.dataId "
                    + "join Tag t on ldt.tagId = t.id "
                    + "where t.tag = :tag and tc.chatId = :chatId and ld.deleted = false")
    List<LinkData> getByTagAndChatIdSync(@Param("tag") String tag, @Param("chatId") long chatId);

    @Query(value = "select l from LinkData l where l.chatId = :chatId and l.linkId = :linkId")
    Optional<LinkData> getByChatIdLinkIdWithDeletedSync(@Param("chatId") long chatId, @Param("linkId") long linkId);

    @Transactional
    default void createSync(LinkData linkData) {
        Optional<LinkData> data = getByChatIdLinkIdWithDeletedSync(linkData.chatId(), linkData.linkId());
        data.ifPresent(l -> linkData.id(l.id()));
        save(linkData);
    }

    @Modifying
    @Transactional
    @Query(value = "update LinkData ld set ld.deleted = true where ld.id = :id")
    void deleteByIdSync(@Param("id") long id);

    @Modifying
    @Transactional
    @Query(value = "update LinkData ld set ld.deleted = true where ld.linkId = :linkId and ld.chatId = :chatId")
    void deleteSync(@Param("chatId") long chatId, @Param("linkId") long linkId);

    @Modifying
    @Transactional
    @Query(value = "update LinkData ld set ld.deleted = true where ld.chatId = :chatId")
    void deleteByChatIdSync(@Param("chatId") long chatId);
}
