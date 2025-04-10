package backend.academy.scrapper.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.dto.Update;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.repository.link.LinkRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UpdatesCheckSchedulerTest {

    protected final int batchSize = 1;

    private final LinkRepository linkRepository = mock(LinkRepository.class);

    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    private final LinksCheckerService checkerService = mock(LinksCheckerService.class);

    private final UpdatesCheckScheduler scheduler =
            new UpdatesCheckScheduler(batchSize, linkRepository, Duration.ZERO, executorService, checkerService);

    @Test
    public void checkUpdatesTest() {
        when(linkRepository.getNotChecked(anyLong(), any(), anyLong()))
                .thenReturn(
                        List.of(
                                new Link(1L, "https://example.com", LocalDateTime.of(2025, 2, 21, 0, 0)),
                                new Link(2L, "https://example2.com", LocalDateTime.of(2025, 2, 21, 0, 0)),
                                new Link(3L, "https://example3.com", LocalDateTime.of(2025, 2, 21, 0, 0))),
                        List.of(new Link(4L, "https://example4.com", LocalDateTime.of(2025, 2, 21, 0, 0))),
                        List.of());
        when(checkerService.getLinkUpdate(any())).thenReturn(List.of(new Update("test", Map.of())));
        InOrder order = inOrder(linkRepository, checkerService);

        scheduler.checkUpdates();

        order.verify(linkRepository).getNotChecked(anyLong(), any(), anyLong());
        checkIterate(order, 3);
        order.verify(linkRepository, times(1)).update(any());
        order.verify(linkRepository).getNotChecked(anyLong(), any(), anyLong());
        checkIterate(order, 1);
        order.verify(linkRepository, times(1)).update(any());
        order.verify(linkRepository).getNotChecked(anyLong(), any(), anyLong());
        order.verifyNoMoreInteractions();
    }

    private void checkIterate(InOrder order, int count) {
        for (int i = 0; i < count; i++) {
            order.verify(checkerService, times(1)).getLinkUpdate(any());
            order.verify(checkerService, times(1)).sendUpdatesForLink(any(), any());
        }
    }
}
