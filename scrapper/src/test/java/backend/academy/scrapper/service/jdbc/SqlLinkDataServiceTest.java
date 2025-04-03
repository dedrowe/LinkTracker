package backend.academy.scrapper.service.jdbc;

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
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.repository.linkdata.LinkDataRepository;
import backend.academy.scrapper.service.FiltersService;
import backend.academy.scrapper.service.TagsService;
import backend.academy.scrapper.service.TgChatService;
import backend.academy.scrapper.service.sql.SqlLinkDataService;
import backend.academy.scrapper.service.sql.SqlLinksCheckerService;
import backend.academy.shared.dto.AddLinkRequest;
import backend.academy.shared.dto.LinkResponse;
import backend.academy.shared.dto.RemoveLinkRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SqlLinkDataServiceTest {

    private final LinkDataRepository linkDataRepository = mock(LinkDataRepository.class);

    private final LinkRepository linkRepository = mock(LinkRepository.class);

    private final FiltersService filtersService = mock(FiltersService.class);

    private final TagsService tagsService = mock(TagsService.class);

    private final TgChatService tgChatService = mock(TgChatService.class);

    private final LinkMapper linkMapper = mock(LinkMapper.class);

    private final SqlLinksCheckerService updatesCheckerService = mock(SqlLinksCheckerService.class);

    private final SqlLinkDataService linkDataService = new SqlLinkDataService(
            linkDataRepository,
            linkRepository,
            filtersService,
            tagsService,
            tgChatService,
            linkMapper,
            updatesCheckerService);

    @BeforeEach
    void setUp() {
        when(tgChatService.getByChatId(Mockito.anyLong())).thenReturn(new TgChat(1L, 123));
        when(linkMapper.createLinkResponse(Mockito.any(), Mockito.anyString(), any(), any()))
                .thenReturn(new LinkResponse(1, "string", List.of(), List.of()));
        when(tagsService.getAllByDataId(anyLong())).thenReturn(CompletableFuture.completedFuture(List.of()));
        when(filtersService.getAllByDataId(anyLong())).thenReturn(CompletableFuture.completedFuture(List.of()));
    }

    @Test
    public void getByChatIdTest() {
        when(linkDataRepository.getByChatId(Mockito.anyLong()))
                .thenReturn(
                        CompletableFuture.completedFuture(List.of(new LinkData(1L, 1L, 1L), new LinkData(2L, 2L, 1L))));
        when(linkRepository.getById(Mockito.anyLong()))
                .thenReturn(
                        CompletableFuture.completedFuture(Optional.of(new Link(1L, "string", LocalDateTime.now()))),
                        CompletableFuture.completedFuture(Optional.of(new Link(2L, "string2", LocalDateTime.now()))));

        linkDataService.getByChatId(1);

        verify(tgChatService, times(1)).getByChatId(Mockito.anyLong());
        verify(linkDataRepository, times(1)).getByChatId(Mockito.anyLong());
        verify(linkRepository, times(2)).getById(Mockito.anyLong());
        verify(tagsService, times(2)).getAllByDataId(anyLong());
        verify(filtersService, times(2)).getAllByDataId(anyLong());
        verify(linkMapper, times(2)).createLinkResponse(Mockito.any(), Mockito.anyString(), any(), any());
    }

    @Test
    public void trackLinkTest() {
        when(linkRepository.getByLink(Mockito.anyString()))
                .thenReturn(
                        CompletableFuture.completedFuture(Optional.empty()),
                        CompletableFuture.completedFuture(Optional.of(new Link(1L, "string", LocalDateTime.now()))));
        when(linkMapper.createLinkData(anyLong(), anyLong())).thenReturn(new LinkData(1L, 1L, 1L));
        when(linkMapper.createLink(anyString())).thenReturn(new Link(1L, "string", LocalDateTime.now()));
        when(linkDataRepository.getByChatIdLinkId(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        when(linkRepository.create(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(linkDataRepository.create(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(tagsService.createAll(any(), anyLong())).thenReturn(CompletableFuture.completedFuture(null));
        when(filtersService.createAll(any(), anyLong())).thenReturn(CompletableFuture.completedFuture(null));

        linkDataService.trackLink(1, new AddLinkRequest("string", List.of(), List.of()));

        verify(tgChatService, times(1)).getByChatId(Mockito.anyLong());
        verify(linkMapper, times(1)).createLink(anyString());
        verify(linkRepository, times(1)).getByLink(Mockito.anyString());
        verify(linkRepository, times(1)).create(Mockito.any());
        verify(linkMapper, times(1)).createLinkData(Mockito.anyLong(), Mockito.anyLong());
        verify(linkDataRepository, times(1)).getByChatIdLinkId(Mockito.anyLong(), Mockito.anyLong());
        verify(linkDataRepository, times(1)).create(Mockito.any());
        verify(tagsService, times(1)).createAll(any(), anyLong());
        verify(filtersService, times(1)).createAll(any(), anyLong());
        verify(tagsService, times(1)).getAllByDataId(anyLong());
        verify(filtersService, times(1)).getAllByDataId(anyLong());
        verify(linkMapper, times(1)).createLinkResponse(Mockito.any(), Mockito.anyString(), any(), any());
    }

    @Test
    public void trackDuplicateLinkTest() {
        when(linkRepository.getByLink(Mockito.anyString()))
                .thenReturn(
                        CompletableFuture.completedFuture(Optional.of(new Link(1L, "string", LocalDateTime.now()))));
        when(linkMapper.createLink(anyString())).thenReturn(new Link(1L, "string", LocalDateTime.now()));
        when(linkMapper.createLinkData(anyLong(), anyLong())).thenReturn(new LinkData(1L, 1L, 1L));
        when(linkDataRepository.getByChatIdLinkId(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(CompletableFuture.completedFuture(Optional.of(new LinkData(1L, 1L, 1L))));
        when(linkRepository.create(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(linkDataRepository.create(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(tagsService.createAll(any(), anyLong())).thenReturn(CompletableFuture.completedFuture(null));
        when(filtersService.createAll(any(), anyLong())).thenReturn(CompletableFuture.completedFuture(null));

        linkDataService.trackLink(1, new AddLinkRequest("string", List.of(), List.of()));

        verify(tgChatService, times(1)).getByChatId(Mockito.anyLong());
        verify(linkMapper, times(1)).createLink(anyString());
        verify(linkRepository, times(1)).getByLink(Mockito.anyString());
        verify(linkMapper, times(1)).createLinkData(Mockito.anyLong(), Mockito.anyLong());
        verify(linkDataRepository, times(1)).getByChatIdLinkId(Mockito.anyLong(), Mockito.anyLong());
        verify(linkDataRepository, times(0)).create(Mockito.any());
        verify(tagsService, times(1)).createAll(any(), anyLong());
        verify(filtersService, times(1)).createAll(any(), anyLong());
        verify(tagsService, times(1)).getAllByDataId(anyLong());
        verify(filtersService, times(1)).getAllByDataId(anyLong());
        verify(linkMapper, times(1)).createLinkResponse(Mockito.any(), Mockito.anyString(), any(), any());
    }

    @Test
    public void untrackLinkTest() {
        when(linkRepository.getByLink(Mockito.anyString()))
                .thenReturn(
                        CompletableFuture.completedFuture(Optional.of(new Link(1L, "string", LocalDateTime.now()))));
        when(linkDataRepository.getByChatIdLinkId(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(CompletableFuture.completedFuture(Optional.of(new LinkData(1L, 1L, 1L))));
        when(linkDataRepository.deleteLinkData(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(tagsService.deleteAllByDataId(anyLong())).thenReturn(CompletableFuture.completedFuture(null));
        when(filtersService.deleteAllByDataId(anyLong())).thenReturn(CompletableFuture.completedFuture(null));

        linkDataService.untrackLink(1, new RemoveLinkRequest("string"));

        verify(tgChatService, times(1)).getByChatId(Mockito.anyLong());
        verify(linkRepository, times(1)).getByLink(Mockito.anyString());
        verify(linkDataRepository, times(1)).getByChatIdLinkId(Mockito.anyLong(), Mockito.anyLong());
        verify(linkDataRepository, times(1)).deleteLinkData(Mockito.any());
        verify(tagsService, times(1)).getAllByDataId(anyLong());
        verify(filtersService, times(1)).getAllByDataId(anyLong());
        verify(tagsService, times(1)).deleteAllByDataId(anyLong());
        verify(filtersService, times(1)).deleteAllByDataId(anyLong());
        verify(linkMapper, times(1)).createLinkResponse(Mockito.any(), Mockito.anyString(), any(), any());
    }

    @Test
    public void getByTagAndChatIdTest() {
        when(linkDataRepository.getByTagAndChatId(any(), Mockito.anyLong()))
                .thenReturn(
                        CompletableFuture.completedFuture(List.of(new LinkData(1L, 1L, 1L), new LinkData(2L, 2L, 1L))));
        when(linkRepository.getById(Mockito.anyLong()))
                .thenReturn(
                        CompletableFuture.completedFuture(Optional.of(new Link(1L, "string", LocalDateTime.now()))),
                        CompletableFuture.completedFuture(Optional.of(new Link(2L, "string2", LocalDateTime.now()))));

        linkDataService.getLinksByTagAndChatId("test", 1L);

        verify(linkDataRepository, times(1)).getByTagAndChatId(anyString(), Mockito.anyLong());
        verify(linkRepository, times(2)).getById(Mockito.anyLong());
        verify(tagsService, times(2)).getAllByDataId(anyLong());
        verify(filtersService, times(2)).getAllByDataId(anyLong());
        verify(linkMapper, times(2)).createLinkResponse(Mockito.any(), Mockito.anyString(), any(), any());
    }
}
