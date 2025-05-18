package backend.academy.scrapper.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.entity.Outbox;
import backend.academy.scrapper.mapper.LinkMapper;
import backend.academy.scrapper.repository.outbox.OutboxRepository;
import backend.academy.scrapper.service.botClient.TgBotClientWrapper;
import backend.academy.scrapper.utils.UtcDateTimeProvider;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UpdatesSendServiceTest {

    private static final int batchSize = 3;

    private final OutboxRepository outboxRepository = mock(OutboxRepository.class);

    private final TgBotClientWrapper clientWrapper = mock(TgBotClientWrapper.class);

    private final LinkMapper linkMapper = mock(LinkMapper.class);

    private final UpdatesSendService service =
            new UpdatesSendService(batchSize, outboxRepository, clientWrapper, linkMapper);

    private final LocalDateTime testTime = UtcDateTimeProvider.now().minusHours(1);

    @Test
    public void sendUpdatesTest() {
        when(outboxRepository.getAllWithDeletion(anyLong()))
                .thenReturn(List.of(
                        new Outbox(1L, "link1", 1L, "test1", testTime),
                        new Outbox(1L, "link1", 2L, "test1", testTime),
                        new Outbox(2L, "link2", 2L, "test2", testTime)));

        InOrder order = inOrder(outboxRepository, clientWrapper, linkMapper);

        service.sendUpdates();

        order.verify(outboxRepository).getAllWithDeletion(anyLong());
        order.verify(linkMapper, times(1))
                .createLinkUpdate(1L, "link1", "Получено обновление по ссылке link1\ntest1", List.of(1L, 2L));
        order.verify(clientWrapper, times(1)).sendUpdates(any());
        order.verify(linkMapper, times(1))
                .createLinkUpdate(2L, "link2", "Получено обновление по ссылке link2\ntest2", List.of(2L));
        order.verify(clientWrapper, times(1)).sendUpdates(any());

        order.verifyNoMoreInteractions();
    }
}
