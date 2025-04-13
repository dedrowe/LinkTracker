package backend.academy.scrapper.repository.outbox;

import backend.academy.scrapper.entity.Outbox;
import java.util.List;
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
    @Modifying
    @Query(
            value =
                    "delete from outbox where id in (select id from outbox where id > :minId limit :limit for update nowait)\n"
                            + "            returning *",
            nativeQuery = true)
    List<Outbox> getAllWithDeletion(@Param("minId") long minId, @Param("limit") long limit);

    @Override
    @Transactional
    default void create(Outbox outbox) {
        save(outbox);
    }
}
