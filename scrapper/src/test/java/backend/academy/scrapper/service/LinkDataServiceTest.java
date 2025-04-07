package backend.academy.scrapper.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.TgChat;
import backend.academy.scrapper.entity.jdbc.JdbcLinkData;
import backend.academy.scrapper.mapper.LinkMapper;
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.repository.linkdata.LinkDataRepository;
import backend.academy.scrapper.service.sql.SqlLinksCheckerService;
import backend.academy.shared.dto.AddLinkRequest;
import backend.academy.shared.dto.LinkResponse;
import backend.academy.shared.dto.RemoveLinkRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LinkDataServiceTest {

    private final LinkDataRepository linkDataRepository = mock(LinkDataRepository.class);

    private final LinkRepository linkRepository = mock(LinkRepository.class);

    private final FiltersService filtersService = mock(FiltersService.class);

    private final TagsService tagsService = mock(TagsService.class);

    private final TgChatService tgChatService = mock(TgChatService.class);

    private final LinkMapper linkMapper = mock(LinkMapper.class);

    private final SqlLinksCheckerService updatesCheckerService = mock(SqlLinksCheckerService.class);

    private final LinkDataService linkDataService = new LinkDataService(
            linkDataRepository,
            linkRepository,
            filtersService,
            tagsService,
            tgChatService,
            linkMapper,
            updatesCheckerService);

    @BeforeEach
    void setUp() {
        when(tgChatService.getByChatId(anyLong())).thenReturn(new TgChat(1L, 123));
        when(linkMapper.createLinkResponse(any(), anyString(), any(), any()))
                .thenReturn(new LinkResponse(1, "string", List.of(), List.of()));
        when(tagsService.getAllByDataId(anyLong())).thenReturn(List.of());
        when(filtersService.getAllByDataId(anyLong())).thenReturn(List.of());
    }

    @Test
    public void getByChatIdTest() {
        when(linkDataRepository.getByChatId(anyLong()))
                .thenReturn(List.of(new JdbcLinkData(1L, 1L, 1L), new JdbcLinkData(2L, 2L, 1L)));
        when(linkRepository.getById(anyLong()))
                .thenReturn(
                        Optional.of(new Link(1L, "string", LocalDateTime.now())),
                        Optional.of(new Link(2L, "string2", LocalDateTime.now())));

        linkDataService.getByChatId(1);

        verify(tgChatService, times(1)).getByChatId(anyLong());
        verify(linkDataRepository, times(1)).getByChatId(anyLong());
        verify(linkRepository, times(2)).getById(anyLong());
        verify(tagsService, times(2)).getAllByDataId(anyLong());
        verify(filtersService, times(2)).getAllByDataId(anyLong());
        verify(linkMapper, times(2)).createLinkResponse(any(), anyString(), any(), any());
    }

    @Test
    public void trackLinkTest() {
        when(linkMapper.createLinkData(any(TgChat.class), any(Link.class))).thenReturn(new JdbcLinkData(1L, 1L, 1L));
        when(linkMapper.createLink(anyString())).thenReturn(new Link(1L, "string", LocalDateTime.now()));
        when(linkDataRepository.getByChatIdLinkId(anyLong(), anyLong())).thenReturn(Optional.empty());

        linkDataService.trackLink(1, new AddLinkRequest("string", List.of(), List.of()));

        verify(tgChatService, times(1)).getByChatId(anyLong());
        verify(updatesCheckerService, times(1)).checkResource(any());
        verify(linkMapper, times(1)).createLink(anyString());
        verify(linkRepository, times(1)).create(any());
        verify(linkDataRepository, times(1)).getByChatIdLinkId(anyLong(), anyLong());
        verify(linkMapper, times(1)).createLinkData(any(), any());
        verify(linkDataRepository, times(1)).create(any());
        verify(tagsService, times(1)).createAll(any(), any());
        verify(filtersService, times(1)).createAll(any(), any());
        verify(linkMapper, times(1)).createLinkResponse(any(), anyString(), any(), any());
    }

    @Test
    public void trackDuplicateLinkTest() {
        when(linkRepository.getByLink(anyString()))
                .thenReturn(Optional.of(new Link(1L, "string", LocalDateTime.now())));
        when(linkMapper.createLink(anyString())).thenReturn(new Link(1L, "string", LocalDateTime.now()));
        when(linkMapper.createLinkData(any(TgChat.class), any(Link.class))).thenReturn(new JdbcLinkData(1L, 1L, 1L));
        when(linkDataRepository.getByChatIdLinkId(anyLong(), anyLong()))
                .thenReturn(Optional.of(new JdbcLinkData(1L, 1L, 1L)));

        linkDataService.trackLink(1, new AddLinkRequest("string", List.of(), List.of()));

        verify(tgChatService, times(1)).getByChatId(anyLong());
        verify(updatesCheckerService, times(1)).checkResource(any());
        verify(linkMapper, times(1)).createLink(anyString());
        verify(linkRepository, times(1)).create(any());
        verify(linkDataRepository, times(1)).getByChatIdLinkId(anyLong(), anyLong());
        verify(tagsService, times(1)).createAll(any(), any());
        verify(filtersService, times(1)).createAll(any(), any());
        verify(linkMapper, times(1)).createLinkResponse(any(), anyString(), any(), any());
    }

    @Test
    public void untrackLinkTest() {
        when(linkRepository.getByLink(anyString()))
                .thenReturn(Optional.of(new Link(1L, "string", LocalDateTime.now())));
        when(linkDataRepository.getByChatIdLinkId(anyLong(), anyLong()))
                .thenReturn(Optional.of(new JdbcLinkData(1L, 1L, 1L)));

        linkDataService.untrackLink(1, new RemoveLinkRequest("string"));

        verify(tgChatService, times(1)).getByChatId(anyLong());
        verify(linkRepository, times(1)).getByLink(anyString());
        verify(linkDataRepository, times(1)).getByChatIdLinkId(anyLong(), anyLong());
        verify(tagsService, times(1)).getAllByDataId(anyLong());
        verify(filtersService, times(1)).getAllByDataId(anyLong());
        verify(linkMapper, times(1)).createLinkResponse(any(), anyString(), any(), any());
        verify(linkDataRepository, times(1)).deleteLinkData(any());
    }

    @Test
    public void getByTagAndChatIdTest() {
        when(linkDataRepository.getByTagAndChatId(any(), anyLong()))
                .thenReturn(List.of(new JdbcLinkData(1L, 1L, 1L), new JdbcLinkData(2L, 2L, 1L)));
        when(linkRepository.getById(anyLong()))
                .thenReturn(
                        Optional.of(new Link(1L, "string", LocalDateTime.now())),
                        Optional.of(new Link(2L, "string2", LocalDateTime.now())));

        linkDataService.getLinksByTagAndChatId("test", 1L);

        verify(linkDataRepository, times(1)).getByTagAndChatId(anyString(), anyLong());
        verify(linkRepository, times(2)).getById(anyLong());
        verify(tagsService, times(2)).getAllByDataId(anyLong());
        verify(filtersService, times(2)).getAllByDataId(anyLong());
        verify(linkMapper, times(2)).createLinkResponse(any(), anyString(), any(), any());
    }
}
