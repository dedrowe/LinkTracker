package backend.academy.scrapper.service.jdbc;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.TgChat;
import backend.academy.scrapper.entity.jdbc.JdbcLinkData;
import backend.academy.scrapper.exceptionHandling.exceptions.WrongServiceException;
import backend.academy.scrapper.mapper.LinkMapper;
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.repository.linkdata.LinkDataRepository;
import backend.academy.scrapper.repository.tgchat.TgChatRepository;
import backend.academy.scrapper.service.LinkDispatcher;
import backend.academy.scrapper.service.apiClient.TgBotClient;
import backend.academy.scrapper.service.apiClient.wrapper.ApiClientWrapper;
import backend.academy.scrapper.service.sql.SqlLinksCheckerService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SqlLinksCheckerServiceTest {

    private final LinkDispatcher linkDispatcher = mock(LinkDispatcher.class);

    private final LinkDataRepository linkDataRepository = mock(LinkDataRepository.class);

    private final LinkRepository linkRepository = mock(LinkRepository.class);

    private final LinkMapper linkMapper = mock(LinkMapper.class);

    private final TgBotClient tgBotClient = mock(TgBotClient.class);

    private final TgChatRepository tgChatRepository = mock(TgChatRepository.class);

    private final ApiClientWrapper clientWrapper = mock(ApiClientWrapper.class);

    private final int batchSize = 3;

    private final SqlLinksCheckerService updatesCheckerService = new SqlLinksCheckerService(
            linkDispatcher, batchSize, linkDataRepository, linkRepository, linkMapper, tgBotClient, tgChatRepository);

    @BeforeEach
    public void setUp() {}

    @Test
    public void notificationsCountTest() {
        InOrder order = inOrder(
                linkRepository,
                linkDispatcher,
                clientWrapper,
                linkDataRepository,
                tgChatRepository,
                linkMapper,
                tgBotClient);
        when(linkDispatcher.dispatchLink(any())).thenReturn(clientWrapper);
        when(clientWrapper.getLastUpdate(any(), any())).thenReturn(Optional.of(""));

        JdbcLinkData linkData = new JdbcLinkData(1L, 1L, 1L);

        when(linkDataRepository.getByLinkId(eq(1L), anyLong(), anyLong()))
                .thenReturn(
                        CompletableFuture.completedFuture(List.of(linkData, linkData, linkData)),
                        CompletableFuture.completedFuture(List.of(linkData)),
                        CompletableFuture.completedFuture(List.of()));
        when(tgChatRepository.getById(anyLong()))
                .thenReturn(CompletableFuture.completedFuture(Optional.of(new TgChat(1L, 123L))));
        when(linkRepository.update(any())).thenReturn(CompletableFuture.completedFuture(null));

        updatesCheckerService.checkUpdatesForLink(new Link(1L, "test", LocalDateTime.now()));

        order.verify(linkDispatcher).dispatchLink(any());
        order.verify(linkDataRepository).getByLinkId(anyLong(), anyLong(), anyLong());
        order.verify(clientWrapper).getLastUpdate(any(), any());

        checkOneLinkIteration(order, 3);
        checkOneLinkIteration(order, 1);

        order.verify(linkRepository).update(any());
        order.verifyNoMoreInteractions();
    }

    @Test
    public void checkValidResourceTest() {
        when(linkDispatcher.dispatchLink(any())).thenReturn(clientWrapper);
        String url = "https://example.com";

        assertThatNoException().isThrownBy(() -> updatesCheckerService.checkResource(url));
    }

    @Test
    public void checkInvalidResourceTest() {
        when(linkDispatcher.dispatchLink(any())).thenReturn(clientWrapper);
        String url = "example.com";

        assertThatThrownBy(() -> updatesCheckerService.checkResource(url)).isInstanceOf(WrongServiceException.class);
    }

    @Test
    public void checkWrongResourceTest() {
        when(linkDispatcher.dispatchLink(any())).thenThrow(new WrongServiceException("test", "test"));
        String url = "https://example.com";

        assertThatThrownBy(() -> updatesCheckerService.checkResource(url)).isInstanceOf(WrongServiceException.class);
    }

    private void checkOneLinkIteration(InOrder order, int chatsCount) {
        order.verify(tgChatRepository, times(chatsCount)).getById(anyLong());
        order.verify(tgBotClient, times(1)).sendUpdates(any());
    }
}
