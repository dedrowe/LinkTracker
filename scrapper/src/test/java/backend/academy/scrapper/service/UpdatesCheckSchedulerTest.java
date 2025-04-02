package backend.academy.scrapper.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.repository.link.LinkRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
        when(linkRepository.getAllNotChecked(anyLong(), any(), anyLong()))
                .thenReturn(
                        CompletableFuture.completedFuture(List.of(
                                new Link(1L, "https://example.com", LocalDateTime.of(2025, 2, 21, 0, 0)),
                                new Link(2L, "https://example2.com", LocalDateTime.of(2025, 2, 21, 0, 0)),
                                new Link(3L, "https://example3.com", LocalDateTime.of(2025, 2, 21, 0, 0)))),
                        CompletableFuture.completedFuture(
                                List.of(new Link(4L, "https://example4.com", LocalDateTime.of(2025, 2, 21, 0, 0)))),
                        CompletableFuture.completedFuture(List.of()));
        InOrder order = inOrder(linkRepository, checkerService);

        scheduler.checkUpdates();

        order.verify(linkRepository).getAllNotChecked(anyLong(), any(), anyLong());
        order.verify(checkerService, times(3)).checkUpdatesForLink(any());
        order.verify(linkRepository).getAllNotChecked(anyLong(), any(), anyLong());
        order.verify(checkerService, times(1)).checkUpdatesForLink(any());
        order.verify(linkRepository).getAllNotChecked(anyLong(), any(), anyLong());
        order.verifyNoMoreInteractions();
    }
}
