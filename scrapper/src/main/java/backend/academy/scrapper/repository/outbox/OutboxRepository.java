package backend.academy.scrapper.repository.outbox;

import backend.academy.scrapper.entity.Outbox;
import java.util.List;

public interface OutboxRepository {

    List<Outbox> getAllWithDeletion(long limit);

    void create(Outbox outbox);
}
