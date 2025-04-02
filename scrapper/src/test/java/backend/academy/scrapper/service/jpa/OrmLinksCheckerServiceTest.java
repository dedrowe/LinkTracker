package backend.academy.scrapper.service.jpa;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.entity.TgChat;
import backend.academy.scrapper.mapper.LinkMapper;
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.service.LinkDispatcher;
import backend.academy.scrapper.service.apiClient.TgBotClient;
import backend.academy.scrapper.service.apiClient.wrapper.ApiClientWrapper;
import backend.academy.scrapper.service.orm.OrmLinksCheckerService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OrmLinksCheckerServiceTest {

    private final LinkDispatcher linkDispatcher = mock(LinkDispatcher.class);

    private final LinkRepository linkRepository = mock(LinkRepository.class);

    private final LinkMapper linkMapper = mock(LinkMapper.class);

    private final TgBotClient tgBotClient = mock(TgBotClient.class);

    private final ApiClientWrapper clientWrapper = mock(ApiClientWrapper.class);

    private final LinkData linkData =
            new LinkData(1L, 1L, 1L, false, new Link("string"), new TgChat(1L), List.of(), List.of());

    private final OrmLinksCheckerService updatesCheckerService =
            new OrmLinksCheckerService(linkDispatcher, linkMapper, tgBotClient, linkRepository);

    @Test
    public void notificationsCountTest() {
        InOrder order = inOrder(linkRepository, linkDispatcher, clientWrapper, linkMapper, tgBotClient);
        when(linkDispatcher.dispatchLink(any())).thenReturn(clientWrapper);
        when(clientWrapper.getLastUpdate(any(), any())).thenReturn(Optional.of(""));
        when(linkRepository.update(any())).thenReturn(CompletableFuture.completedFuture(null));

        updatesCheckerService.checkUpdatesForLink(new Link(1L, "link", LocalDateTime.now(), false, List.of(linkData)));

        order.verify(linkDispatcher).dispatchLink(any());
        order.verify(clientWrapper).getLastUpdate(any(), any());
        order.verify(tgBotClient).sendUpdates(any());
        order.verify(linkRepository).update(any());
        order.verifyNoMoreInteractions();
    }
}
