package backend.academy.scrapper.service.jpa;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.entity.TgChat;
import backend.academy.scrapper.mapper.LinkMapper;
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.repository.linkdata.LinkDataRepository;
import backend.academy.scrapper.repository.tgchat.TgChatRepository;
import backend.academy.scrapper.service.LinkDispatcher;
import backend.academy.scrapper.service.apiClient.TgBotClient;
import backend.academy.scrapper.service.apiClient.wrapper.GithubWrapper;
import backend.academy.scrapper.service.orm.OrmUpdatesCheckerService;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OrmUpdatesCheckerServiceTest {

    @Mock
    private LinkDispatcher linkDispatcher;

    @Mock
    private LinkDataRepository linkDataRepository;

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private LinkMapper linkMapper;

    @Mock
    private TgBotClient tgBotClient;

    @Mock
    private TgChatRepository tgChatRepository;

    @Mock
    private GithubWrapper githubClient;

    private final int batchSize = 3;

    private final int threadsSize = 1;

    private OrmUpdatesCheckerService updatesCheckerService;

    @BeforeEach
    public void setUp() {
        updatesCheckerService = new OrmUpdatesCheckerService(
                batchSize,
                threadsSize,
                linkDispatcher,
                linkDataRepository,
                linkRepository,
                linkMapper,
                tgBotClient,
                tgChatRepository,
                Duration.ZERO);
        when(linkRepository.getAllNotChecked(anyLong(), anyLong(), any(), anyLong()))
                .thenReturn(
                        CompletableFuture.completedFuture(List.of(
                                new Link(1L, "https://example.com", LocalDateTime.of(2025, 2, 21, 0, 0)),
                                new Link(2L, "https://example2.com", LocalDateTime.of(2025, 2, 21, 0, 0)),
                                new Link(3L, "https://example3.com", LocalDateTime.of(2025, 2, 21, 0, 0)))),
                        CompletableFuture.completedFuture(
                                List.of(new Link(4L, "https://example4.com", LocalDateTime.of(2025, 2, 21, 0, 0)))),
                        CompletableFuture.completedFuture(List.of()));
        when(linkDispatcher.dispatchLink(any())).thenReturn(githubClient);
        when(githubClient.getLastUpdate(any(), any())).thenReturn(Optional.of(""));

        LinkData linkData = new LinkData(1L, 1L, 1L, false, new Link("string"), new TgChat(1L), List.of(), List.of());

        when(linkDataRepository.getByLinkId(eq(1L), anyLong(), anyLong()))
                .thenReturn(
                        CompletableFuture.completedFuture(List.of(linkData)),
                        CompletableFuture.completedFuture(List.of()));
        when(linkDataRepository.getByLinkId(eq(2L), anyLong(), anyLong()))
                .thenReturn(
                        CompletableFuture.completedFuture(List.of(linkData, linkData, linkData)),
                        CompletableFuture.completedFuture(List.of(linkData)),
                        CompletableFuture.completedFuture(List.of()));
        when(linkDataRepository.getByLinkId(eq(3L), anyLong(), anyLong()))
                .thenReturn(
                        CompletableFuture.completedFuture(List.of(linkData, linkData, linkData)),
                        CompletableFuture.completedFuture(List.of()));
        when(linkDataRepository.getByLinkId(eq(4L), anyLong(), anyLong()))
                .thenReturn(
                        CompletableFuture.completedFuture(List.of(linkData, linkData)),
                        CompletableFuture.completedFuture(List.of()));

        when(linkRepository.update(any())).thenReturn(CompletableFuture.completedFuture(null));
    }

    @Test
    public void notificationsCountTest() {
        InOrder order = inOrder(
                linkRepository,
                linkDispatcher,
                githubClient,
                linkDataRepository,
                tgChatRepository,
                linkMapper,
                tgBotClient);

        updatesCheckerService.checkUpdates();

        order.verify(linkRepository).getAllNotChecked(anyLong(), anyLong(), any(), anyLong());

        checkOneLinkIteration(order, 1);
        checkOneLinkIteration(order, 4);
        checkOneLinkIteration(order, 3);

        order.verify(linkRepository).getAllNotChecked(anyLong(), anyLong(), any(), anyLong());

        checkOneLinkIteration(order, 2);

        order.verify(linkRepository).getAllNotChecked(anyLong(), anyLong(), any(), anyLong());

        order.verifyNoMoreInteractions();
    }

    private void checkOneLinkIteration(InOrder order, int chatsCount) {
        order.verify(linkDispatcher).dispatchLink(any());
        order.verify(linkDataRepository).getByLinkId(anyLong(), anyLong(), anyLong());
        order.verify(githubClient).getLastUpdate(any(), any());

        int loopsCount = Math.ceilDiv(chatsCount, batchSize);
        for (int i = 0; i < loopsCount; i++) {
            order.verify(linkMapper).createLinkUpdate(anyLong(), anyString(), anyString(), any());
            order.verify(tgBotClient).sendUpdates(any());
            order.verify(linkDataRepository).getByLinkId(anyLong(), anyLong(), anyLong());
        }
        order.verify(linkRepository).update(any());
    }
}
