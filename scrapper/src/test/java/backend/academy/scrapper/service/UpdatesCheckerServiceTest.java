package backend.academy.scrapper.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.entity.TgChat;
import backend.academy.scrapper.mapper.LinkMapper;
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.repository.linkdata.LinkDataRepository;
import backend.academy.scrapper.service.apiClient.TgBotClient;
import backend.academy.scrapper.service.apiClient.wrapper.GithubWrapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UpdatesCheckerServiceTest {

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
    private TgChatService tgChatService;

    @Mock
    private GithubWrapper githubClient;

    @InjectMocks
    private UpdatesCheckerService updatesCheckerService;

    @BeforeEach
    public void setUp() {
        when(linkRepository.getAll())
                .thenReturn(CompletableFuture.completedFuture(List.of(
                        new Link(1L, "https://example.com", LocalDateTime.of(2025, 2, 21, 0, 0)),
                        new Link(2L, "https://example2.com", LocalDateTime.of(2025, 2, 21, 0, 0)))));
        when(linkDispatcher.dispatchLink(any())).thenReturn(githubClient);
        when(githubClient.getLastUpdate(any())).thenReturn(LocalDateTime.of(2025, 2, 22, 0, 0));
        when(linkDataRepository.getByLinkId(anyLong()))
                .thenReturn(
                        CompletableFuture.completedFuture(List.of(new LinkData(1L, 1L, 1L, List.of(), List.of()))),
                        CompletableFuture.completedFuture(List.of(
                                new LinkData(1L, 1L, 1L, List.of(), List.of()),
                                new LinkData(1L, 1L, 1L, List.of(), List.of()))));
        when(tgChatService.getById(anyLong())).thenReturn(new TgChat(1L, 123L));
    }

    @Test
    public void notificationsCountTest() {
        InOrder order = inOrder(
                linkRepository,
                linkDispatcher,
                githubClient,
                linkDataRepository,
                tgChatService,
                linkMapper,
                tgBotClient);

        updatesCheckerService.checkUpdates();

        order.verify(linkRepository).getAll();

        order.verify(linkDispatcher).dispatchLink(any());
        order.verify(githubClient).getLastUpdate(any());
        order.verify(linkRepository).update(any());
        order.verify(linkDataRepository).getByLinkId(anyLong());
        order.verify(tgChatService, times(1)).getById(anyLong());
        order.verify(linkMapper).createLinkUpdate(anyLong(), anyString(), anyString(), any());
        order.verify(tgBotClient).sendUpdates(any());

        order.verify(linkDispatcher).dispatchLink(any());
        order.verify(githubClient).getLastUpdate(any());
        order.verify(linkRepository).update(any());
        order.verify(linkDataRepository).getByLinkId(anyLong());
        order.verify(tgChatService, times(2)).getById(anyLong());
        order.verify(linkMapper).createLinkUpdate(anyLong(), anyString(), anyString(), any());
        order.verify(tgBotClient).sendUpdates(any());
    }
}
