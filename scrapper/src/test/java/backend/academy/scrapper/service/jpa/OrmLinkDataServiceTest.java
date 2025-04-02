package backend.academy.scrapper.service.jpa;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.entity.TgChat;
import backend.academy.scrapper.mapper.LinkMapper;
import backend.academy.scrapper.repository.filters.JdbcFiltersRepository;
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.repository.linkdata.LinkDataRepository;
import backend.academy.scrapper.repository.tags.JdbcTagsRepository;
import backend.academy.scrapper.service.LinkDataService;
import backend.academy.scrapper.service.TgChatService;
import backend.academy.scrapper.service.orm.OrmLinkDataService;
import backend.academy.scrapper.service.sql.SqlLinksCheckerService;
import backend.academy.shared.dto.AddLinkRequest;
import backend.academy.shared.dto.LinkResponse;
import backend.academy.shared.dto.RemoveLinkRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OrmLinkDataServiceTest {

    private final LinkDataRepository linkDataRepository = mock(LinkDataRepository.class);

    private final LinkRepository linkRepository = mock(LinkRepository.class);

    private final JdbcFiltersRepository filtersRepository = mock(JdbcFiltersRepository.class);

    private final JdbcTagsRepository tagsRepository = mock(JdbcTagsRepository.class);

    private final TgChatService tgChatService = mock(TgChatService.class);

    private final LinkMapper linkMapper = mock(LinkMapper.class);

    private final SqlLinksCheckerService updatesCheckerService = mock(SqlLinksCheckerService.class);

    private final LinkDataService linkDataService = new OrmLinkDataService(
            linkDataRepository,
            linkRepository,
            filtersRepository,
            tagsRepository,
            tgChatService,
            linkMapper,
            updatesCheckerService);

    @BeforeEach
    void setUp() {
        when(tgChatService.getByChatId(anyLong())).thenReturn(new TgChat(1L, 123));
        when(linkMapper.createLinkResponse(any(), anyString(), any(), any()))
                .thenReturn(new LinkResponse(1, "string", List.of(), List.of()));
        when(tagsRepository.getAllByDataId(anyLong())).thenReturn(CompletableFuture.completedFuture(List.of()));
        when(filtersRepository.getAllByDataId(anyLong())).thenReturn(CompletableFuture.completedFuture(List.of()));
    }

    @Test
    public void getByChatIdTest() {
        Link link1 = new Link(1L, "string", LocalDateTime.now());
        Link link2 = new Link(2L, "string2", LocalDateTime.now());
        TgChat tgChat1 = new TgChat(1L, 123);
        when(linkDataRepository.getByChatId(anyLong()))
                .thenReturn(CompletableFuture.completedFuture(List.of(
                        new LinkData(1L, 1L, 1L, false, link1, tgChat1, List.of(), List.of()),
                        new LinkData(2L, 2L, 1L, false, link1, tgChat1, List.of(), List.of()))));
        when(linkRepository.getById(anyLong()))
                .thenReturn(
                        CompletableFuture.completedFuture(Optional.of(link1)),
                        CompletableFuture.completedFuture(Optional.of(link2)));

        linkDataService.getByChatId(1);

        verify(tgChatService, times(1)).getByChatId(anyLong());
        verify(linkDataRepository, times(1)).getByChatId(anyLong());
        verify(linkMapper, times(2)).createLinkResponse(any(), anyString(), any(), any());
    }

    @Test
    public void trackLinkTest() {
        when(linkMapper.createLink(anyString())).thenReturn(new Link(1L, "string", LocalDateTime.now()));
        when(linkRepository.create(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(linkDataRepository.create(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(tagsRepository.getAllByTagsSet(any())).thenReturn(CompletableFuture.completedFuture(List.of()));
        when(linkDataRepository.getByChatIdLinkId(anyLong(), anyLong()))
                .thenReturn(CompletableFuture.completedFuture(Optional.of(new LinkData(
                        1L, 1L, 1L, false, new Link("string"), new TgChat(1L), new ArrayList<>(), new ArrayList<>()))));

        linkDataService.trackLink(1, new AddLinkRequest("string", List.of(), List.of()));

        verify(tgChatService, times(1)).getByChatId(anyLong());
        verify(tagsRepository, times(1)).getAllByTagsSet(any());
        verify(linkMapper, times(1)).createLink(anyString());
        verify(linkRepository, times(1)).create(any());
        verify(linkDataRepository, times(1)).getByChatIdLinkId(anyLong(), anyLong());
        verify(linkDataRepository, times(1)).create(any());
        verify(linkMapper, times(1)).createLinkResponse(any(), anyString(), any(), any());
    }

    @Test
    public void untrackLinkTest() {
        when(linkRepository.getByLink(Mockito.anyString()))
                .thenReturn(
                        CompletableFuture.completedFuture(Optional.of(new Link(1L, "string", LocalDateTime.now()))));
        when(linkDataRepository.getByChatIdLinkId(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(CompletableFuture.completedFuture(Optional.of(new LinkData(1L, 1L, 1L))));
        when(linkDataRepository.deleteLinkData(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(tagsRepository.deleteAllByDataId(anyLong())).thenReturn(CompletableFuture.completedFuture(null));
        when(filtersRepository.deleteAllByDataId(anyLong())).thenReturn(CompletableFuture.completedFuture(null));

        linkDataService.untrackLink(1, new RemoveLinkRequest("string"));

        verify(tgChatService, times(1)).getByChatId(anyLong());
        verify(linkRepository, times(1)).getByLink(Mockito.anyString());
        verify(linkDataRepository, times(1)).getByChatIdLinkId(Mockito.anyLong(), Mockito.anyLong());
        verify(linkMapper, times(1)).createLinkResponse(any(), anyString(), any(), any());
        verify(linkDataRepository, times(1)).deleteLinkData(Mockito.any());
    }

    @Test
    public void getByTagAndChatIdTest() {
        when(linkDataRepository.getByTagAndChatId(anyString(), anyLong()))
                .thenReturn(CompletableFuture.completedFuture(List.of(
                        new LinkData(1L, 1L, 1L, false, new Link("string"), new TgChat(1L), List.of(), List.of()))));
        when(linkMapper.createLinkResponse(any(), anyString(), any(), any()))
                .thenReturn(new LinkResponse(1L, "string", List.of(), List.of()));

        linkDataService.getLinksByTagAndChatId("test", 1L);

        verify(linkDataRepository, times(1)).getByTagAndChatId(anyString(), anyLong());
        verify(linkMapper, times(1)).createLinkResponse(any(), anyString(), any(), any());
    }
}
