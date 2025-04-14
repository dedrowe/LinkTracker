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

import backend.academy.scrapper.dto.Update;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.Outbox;
import backend.academy.scrapper.entity.TgChat;
import backend.academy.scrapper.entity.jdbc.JdbcFilter;
import backend.academy.scrapper.entity.jdbc.JdbcLinkData;
import backend.academy.scrapper.exceptionHandling.exceptions.WrongServiceException;
import backend.academy.scrapper.repository.linkdata.LinkDataRepository;
import backend.academy.scrapper.repository.outbox.OutboxRepository;
import backend.academy.scrapper.repository.tgchat.TgChatRepository;
import backend.academy.scrapper.service.FiltersService;
import backend.academy.scrapper.service.LinkDispatcher;
import backend.academy.scrapper.service.apiClient.wrapper.ApiClientWrapper;
import backend.academy.scrapper.service.sql.SqlLinksCheckerService;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SqlLinksCheckerServiceTest {

    private final LinkDispatcher linkDispatcher = mock(LinkDispatcher.class);

    private final LinkDataRepository linkDataRepository = mock(LinkDataRepository.class);

    private final TgChatRepository tgChatRepository = mock(TgChatRepository.class);

    private final ApiClientWrapper clientWrapper = mock(ApiClientWrapper.class);

    private final int batchSize = 3;

    private final FiltersService filtersService = mock(FiltersService.class);

    private final OutboxRepository outboxRepository = mock(OutboxRepository.class);

    private final LocalDateTime testDateTime = LocalDateTime.now(ZoneOffset.UTC).minusHours(2);

    private final LocalTime testTime = testDateTime.toLocalTime();

    private final SqlLinksCheckerService updatesCheckerService = new SqlLinksCheckerService(
            linkDispatcher, batchSize, linkDataRepository, tgChatRepository, filtersService, outboxRepository);

    @Test
    public void notificationsCountTest() {
        JdbcLinkData linkData = new JdbcLinkData(1L, 1L, 1L);

        when(linkDataRepository.getByLinkId(eq(1L), anyLong(), anyLong()))
                .thenReturn(List.of(linkData, linkData, linkData), List.of(linkData), List.of());
        when(tgChatRepository.getAllByIds(any())).thenReturn(List.of(new TgChat(1L, 123L, false, List.of(), testTime)));
        when(filtersService.getAllByDataIds(any())).thenReturn(List.of());

        updatesCheckerService.setUpdatesForLink(
                new Link(1L, "test", LocalDateTime.now()),
                List.of(new Update("test", Map.of()), new Update("test", Map.of())));

        InOrder order = inOrder(linkDataRepository, filtersService, tgChatRepository, outboxRepository);

        order.verify(linkDataRepository).getByLinkId(anyLong(), anyLong(), anyLong());
        order.verify(filtersService).getAllByDataIds(any());
        order.verify(tgChatRepository).getAllByIds(any());
        order.verify(outboxRepository, times(8)).create(new Outbox(1L, "test", 123L, "test", testDateTime.plusDays(1)));
        order.verify(linkDataRepository).getByLinkId(anyLong(), anyLong(), anyLong());

        order.verifyNoMoreInteractions();
    }

    @Test
    public void notificationsFilteredCountTest() {

        JdbcLinkData linkData1 = new JdbcLinkData(1L, 1L, 1L);
        JdbcLinkData linkData2 = new JdbcLinkData(2L, 1L, 1L);

        when(linkDataRepository.getByLinkId(eq(1L), anyLong(), anyLong()))
                .thenReturn(List.of(linkData1, linkData2, linkData1), List.of(linkData1), List.of());
        when(tgChatRepository.getAllByIds(any())).thenReturn(List.of(new TgChat(1L, 123L, false, List.of(), testTime)));
        when(filtersService.getAllByDataIds(any())).thenReturn(List.of(new JdbcFilter(2L, "test:test")));

        updatesCheckerService.setUpdatesForLink(
                new Link(1L, "test", LocalDateTime.now()), List.of(new Update("test", Map.of("test", "test"))));

        InOrder order = inOrder(linkDataRepository, filtersService, tgChatRepository, outboxRepository);

        order.verify(linkDataRepository).getByLinkId(anyLong(), anyLong(), anyLong());
        order.verify(filtersService).getAllByDataIds(any());
        order.verify(tgChatRepository).getAllByIds(any());
        order.verify(outboxRepository, times(2)).create(new Outbox(1L, "test", 123L, "test", testDateTime.plusDays(1)));
        order.verify(linkDataRepository).getByLinkId(anyLong(), anyLong(), anyLong());
        order.verify(outboxRepository, times(1)).create(new Outbox(1L, "test", 123L, "test", testDateTime.plusDays(1)));
        order.verify(linkDataRepository).getByLinkId(anyLong(), anyLong(), anyLong());

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
}
