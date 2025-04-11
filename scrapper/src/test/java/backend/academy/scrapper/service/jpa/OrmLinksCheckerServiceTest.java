package backend.academy.scrapper.service.jpa;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.dto.Update;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.Outbox;
import backend.academy.scrapper.entity.TgChat;
import backend.academy.scrapper.entity.jpa.JpaFilter;
import backend.academy.scrapper.entity.jpa.JpaLinkData;
import backend.academy.scrapper.repository.linkdata.JpaLinkDataRepository;
import backend.academy.scrapper.repository.outbox.OutboxRepository;
import backend.academy.scrapper.service.LinkDispatcher;
import backend.academy.scrapper.service.apiClient.wrapper.ApiClientWrapper;
import backend.academy.scrapper.service.orm.OrmLinksCheckerService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OrmLinksCheckerServiceTest {

    private final LinkDispatcher linkDispatcher = mock(LinkDispatcher.class);

    private final ApiClientWrapper clientWrapper = mock(ApiClientWrapper.class);

    private final JpaLinkDataRepository jpaLinkDataRepository = mock(JpaLinkDataRepository.class);

    private final OutboxRepository outboxRepository = mock(OutboxRepository.class);

    private final JpaLinkData linkData = new JpaLinkData(
            1L,
            1L,
            1L,
            false,
            new Link("string"),
            new TgChat(1L),
            List.of(new JpaFilter(null, "user:test")),
            List.of());

    private final OrmLinksCheckerService updatesCheckerService =
            new OrmLinksCheckerService(linkDispatcher, jpaLinkDataRepository, outboxRepository);

    @Test
    public void notificationsCountTest() {
        InOrder order = inOrder(linkDispatcher, clientWrapper, outboxRepository, jpaLinkDataRepository);
        when(linkDispatcher.dispatchLink(any())).thenReturn(clientWrapper);
        when(jpaLinkDataRepository.fetchFilters(any())).thenReturn(List.of(linkData));

        updatesCheckerService.setUpdatesForLink(
                new Link(1L, "link", LocalDateTime.now(), false, false, List.of(linkData)),
                List.of(new Update("test", Map.of()), new Update("test", Map.of())));

        order.verify(jpaLinkDataRepository).fetchFilters(any());
        order.verify(outboxRepository, times(2)).create(new Outbox(1L, "link", 1L, "test"));
        order.verifyNoMoreInteractions();
    }

    @Test
    public void notificationsFilteredCountTest() {
        InOrder order = inOrder(linkDispatcher, clientWrapper, outboxRepository, jpaLinkDataRepository);
        when(linkDispatcher.dispatchLink(any())).thenReturn(clientWrapper);
        when(jpaLinkDataRepository.fetchFilters(any())).thenReturn(List.of(linkData));

        updatesCheckerService.setUpdatesForLink(
                new Link(1L, "link", LocalDateTime.now(), false, false, List.of(linkData)),
                List.of(new Update("test", Map.of("user", "test")), new Update("test", Map.of("user", "test1"))));

        order.verify(jpaLinkDataRepository).fetchFilters(any());
        order.verify(outboxRepository, times(1)).create(new Outbox(1L, "link", 1L, "test"));
        order.verifyNoMoreInteractions();
    }
}
