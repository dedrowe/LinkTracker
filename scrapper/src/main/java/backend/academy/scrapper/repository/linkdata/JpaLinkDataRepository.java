package backend.academy.scrapper.repository.linkdata;

import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.entity.jpa.JpaLinkData;
import java.util.List;
import java.util.Optional;
import org.hibernate.Hibernate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@ConditionalOnProperty(havingValue = "ORM", prefix = "app", name = "access-type")
public interface JpaLinkDataRepository extends LinkDataRepository, JpaRepository<JpaLinkData, Long> {

    @Override
    @Query(value = "select l from JpaLinkData l where l.deleted = false")
    List<JpaLinkData> getAll();

    @Override
    @Query(value = "select l from JpaLinkData l where l.id = :id and l.deleted = false")
    Optional<JpaLinkData> getById(long id);

    @Override
    @Transactional
    default List<JpaLinkData> getByChatId(long chatId) {
        List<JpaLinkData> linksData = getByChatIdInternal(chatId);
        linksData.forEach(this::fetchProperties);
        return linksData;
    }

    @Query(value = "select l from JpaLinkData l where l.chatId = :chatId and l.deleted = false")
    List<JpaLinkData> getByChatIdInternal(@Param("chatId") long chatId);

    @Override
    @Query(value = "select l from JpaLinkData l where l.linkId = :linkId and l.deleted = false")
    List<JpaLinkData> getByLinkId(long linkId);

    @Override
    @Query(
            value = "select * from links_data where link_id = :linkId and deleted = false and id > :minId limit :limit",
            nativeQuery = true)
    List<JpaLinkData> getByLinkId(@Param("linkId") long linkId, @Param("minId") long minId, @Param("limit") long limit);

    @Override
    @Query(value = "select l from JpaLinkData l where l.chatId = :chatId and l.linkId = :linkId and l.deleted = false")
    Optional<JpaLinkData> getByChatIdLinkId(@Param("chatId") long chatId, @Param("linkId") long linkId);

    @Override
    @Query(
            value = "select ld from JpaLinkData ld " + "join TgChat tc on ld.chatId = tc.id "
                    + "join LinkDataToTag ldt on ld.id = ldt.dataId "
                    + "join Tag t on ldt.tagId = t.id "
                    + "where t.tag = :tag and tc.chatId = :chatId and ld.deleted = false")
    List<JpaLinkData> getByTagAndChatId(@Param("tag") String tag, @Param("chatId") long chatId);

    @Override
    @Transactional
    default void create(LinkData linkData) {
        Optional<JpaLinkData> data = getByChatIdLinkIdWithDeletedSync(linkData.chatId(), linkData.linkId());
        data.ifPresent(l -> linkData.id(l.id()));
        save((JpaLinkData) linkData);
        flush();
    }

    @Query(value = "select * from links_data where chat_id = :chatId and link_id = :linkId", nativeQuery = true)
    Optional<JpaLinkData> getByChatIdLinkIdWithDeletedSync(@Param("chatId") long chatId, @Param("linkId") long linkId);

    @Override
    @Transactional
    @Modifying
    @Query(value = "update JpaLinkData ld set ld.deleted = true where ld.id = :id")
    void deleteById(@Param("id") long id);

    @Override
    @Transactional
    default void deleteLinkData(LinkData link) {
        deleteInternal(link.chatId(), link.linkId());
    }

    @Modifying
    @Transactional
    @Query(value = "update JpaLinkData ld set ld.deleted = true where ld.linkId = :linkId and ld.chatId = :chatId")
    void deleteInternal(@Param("chatId") long chatId, @Param("linkId") long linkId);

    @Override
    @Modifying
    @Transactional
    @Query(value = "update JpaLinkData ld set ld.deleted = true where ld.chatId = :chatId")
    void deleteByChatId(@Param("chatId") long chatId);

    @Transactional
    default void fetchProperties(JpaLinkData linkData) {
        Hibernate.initialize(linkData.tags());
        Hibernate.initialize(linkData.filters());
    }
}
