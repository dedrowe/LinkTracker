package backend.academy.scrapper.repository.outbox;

import backend.academy.scrapper.entity.Outbox;
import java.time.LocalDateTime;
import java.util.List;
import backend.academy.scrapper.utils.UtcDateTimeProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@ConditionalOnProperty(havingValue = "ORM", prefix = "app", name = "access-type")
public interface JpaOutboxRepository extends OutboxRepository, CrudRepository<Outbox, Long> {

    @Override
    @Transactional
    default List<Outbox> getAllWithDeletion(long limit) {
        return getAllWithDeletion(limit, UtcDateTimeProvider.now());
    }

    @Transactional
    @Modifying
    @Query(
            value =
                    "delete from outbox where id in (select id from outbox where send_time <= :curTime limit :limit for update skip locked)\n"
                            + "            returning *",
            nativeQuery = true)
    List<Outbox> getAllWithDeletion(@Param("limit") long limit, @Param("curTime") LocalDateTime curTime);

    @Override
    @Transactional
    default void create(Outbox outbox) {
        save(outbox);
    }
}
