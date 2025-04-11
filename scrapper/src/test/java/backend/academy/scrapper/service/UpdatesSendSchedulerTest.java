package backend.academy.scrapper.service;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.entity.Outbox;
import backend.academy.scrapper.mapper.LinkMapper;
import backend.academy.scrapper.repository.outbox.OutboxRepository;
import backend.academy.scrapper.service.apiClient.TgBotClient;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UpdatesSendSchedulerTest {

    private static final int batchSize = 3;

    private final OutboxRepository outboxRepository = mock(OutboxRepository.class);

    private final TgBotClient tgBotClient = mock(TgBotClient.class);

    private final LinkMapper linkMapper = mock(LinkMapper.class);

    private final UpdatesSendScheduler scheduler =
            new UpdatesSendScheduler(batchSize, outboxRepository, tgBotClient, linkMapper);

    @Test
    public void sendUpdatesTest() {
        when(outboxRepository.getAllWithDeletion(anyLong(), anyLong()))
                .thenReturn(
                        List.of(
                                new Outbox(1L, "link1", 1L, "test1"),
                                new Outbox(1L, "link1", 2L, "test1"),
                                new Outbox(2L, "link2", 2L, "test2")),
                        List.of());

        InOrder order = inOrder(outboxRepository, tgBotClient, linkMapper);

        scheduler.sendUpdates();

        order.verify(outboxRepository).getAllWithDeletion(anyLong(), anyLong());
        order.verify(linkMapper, times(1))
                .createLinkUpdate(1L, "link1", "Получено обновление по ссылке link1\ntest1", List.of(1L, 2L));
        order.verify(linkMapper, times(1))
                .createLinkUpdate(2L, "link2", "Получено обновление по ссылке link2\ntest2", List.of(2L));
        order.verify(outboxRepository).getAllWithDeletion(anyLong(), anyLong());

        order.verifyNoMoreInteractions();
    }
}
