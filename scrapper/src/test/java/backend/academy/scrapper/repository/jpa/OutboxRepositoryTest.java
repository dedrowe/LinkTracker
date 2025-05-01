package backend.academy.scrapper.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.entity.Outbox;
import backend.academy.scrapper.repository.outbox.JpaOutboxRepository;
import backend.academy.scrapper.utils.UtcDateTimeProvider;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

public class OutboxRepositoryTest extends AbstractJpaTest {

    private final JpaOutboxRepository repository;

    private final LocalDateTime testTime = UtcDateTimeProvider.now();

    private final Outbox outbox1 = new Outbox(1, "https://example.com", 1, "test description", testTime.plusHours(1));

    private final Outbox outbox2 = new Outbox(1, "https://example.com", 2, "test description", testTime.minusHours(1));

    @Autowired
    public OutboxRepositoryTest(TestEntityManager entityManager, JpaOutboxRepository repository) {
        super(entityManager);
        this.repository = repository;
    }

    @BeforeEach
    public void setUp() {
        entityManager
                .getEntityManager()
                .createNativeQuery("alter sequence outbox_id_seq restart with 1")
                .executeUpdate();

        entityManager.persist(outbox1);
        entityManager.persist(outbox2);
        entityManager.flush();
    }

    @Test
    public void getAllWithDeletionTest() {
        Outbox expectedResult = new Outbox(1, "https://example.com", 2, "test description", testTime.minusHours(1));

        List<Outbox> actualResult = repository.getAllWithDeletion(10);

        assertThat(actualResult).containsExactly(expectedResult);
        assertThat(entityManager
                        .getEntityManager()
                        .createNativeQuery("select count(*) from outbox")
                        .getSingleResult())
                .isEqualTo(1L);
    }

    @Test
    public void createTest() {
        long expectedId = 3;
        Outbox outbox = new Outbox(1, "https://example.com", 3, "test description", testTime);

        repository.create(outbox);

        assertThat(outbox.id()).isEqualTo(expectedId);
    }
}
