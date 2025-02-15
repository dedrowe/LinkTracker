package backend.academy.scrapper.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.dto.LinkResponse;
import backend.academy.scrapper.dto.RemoveLinkRequest;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.entity.TgChat;
import backend.academy.scrapper.mapper.LinkMapper;
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.repository.linkdata.LinkDataRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LinkDataServiceTest {

    private final LinkDataRepository linkDataRepository = mock(LinkDataRepository.class);

    private final LinkRepository linkRepository = mock(LinkRepository.class);

    private final TgChatService tgChatService = mock(TgChatService.class);

    private final LinkMapper linkMapper = mock(LinkMapper.class);

    private final UpdatesCheckerService updatesCheckerService = mock(UpdatesCheckerService.class);

    private final LinkDataService linkDataService =
            new LinkDataService(linkDataRepository, linkRepository, tgChatService, linkMapper, updatesCheckerService);

    @Test
    public void getByChatIdTest() {
        when(tgChatService.getByChatId(Mockito.anyLong())).thenReturn(new TgChat(1L, 123));
        when(linkDataRepository.getByChatId(Mockito.anyLong())).thenReturn(List.of(new LinkData(), new LinkData()));
        when(linkRepository.getById(Mockito.anyLong()))
                .thenReturn(Optional.of(new Link(1L, "string", LocalDateTime.now())));
        when(linkMapper.createLinkResponse(Mockito.any(), Mockito.anyString()))
                .thenReturn(new LinkResponse(1, "string", List.of(), List.of()));

        linkDataService.getByChatId(1);

        verify(tgChatService, times(1)).getByChatId(Mockito.anyLong());
        verify(linkDataRepository, times(1)).getByChatId(Mockito.anyLong());
        verify(linkRepository, times(2)).getById(Mockito.anyLong());
        verify(linkMapper, times(2)).createLinkResponse(Mockito.any(), Mockito.anyString());
    }

    @Test
    public void trackLinkTest() {
        when(tgChatService.getByChatId(Mockito.anyLong())).thenReturn(new TgChat(1L, 123));
        when(linkRepository.getByLink(Mockito.anyString()))
                .thenReturn(Optional.empty(), Optional.of(new Link(1L, "string", LocalDateTime.now())));
        when(linkMapper.createLinkData(Mockito.any(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(new LinkData());
        when(linkDataRepository.getByChatIdLinkId(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(Optional.empty());
        when(linkMapper.createLinkResponse(Mockito.any(), Mockito.anyString()))
                .thenReturn(new LinkResponse(1, "string", List.of(), List.of()));

        linkDataService.trackLink(1, new AddLinkRequest("string", List.of(), List.of()));

        verify(tgChatService, times(1)).getByChatId(Mockito.anyLong());
        verify(linkRepository, times(2)).getByLink(Mockito.anyString());
        verify(linkRepository, times(1)).create(Mockito.any());
        verify(linkMapper, times(1)).createLinkData(Mockito.any(), Mockito.anyLong(), Mockito.anyLong());
        verify(linkDataRepository, times(1)).getByChatIdLinkId(Mockito.anyLong(), Mockito.anyLong());
        verify(linkDataRepository, times(1)).create(Mockito.any());
        verify(linkMapper, times(1)).createLinkResponse(Mockito.any(), Mockito.anyString());
    }

    @Test
    public void trackDuplicateLinkTest() {
        when(tgChatService.getByChatId(Mockito.anyLong())).thenReturn(new TgChat(1L, 123));
        when(linkRepository.getByLink(Mockito.anyString()))
                .thenReturn(Optional.of(new Link(1L, "string", LocalDateTime.now())));
        when(linkMapper.createLinkData(Mockito.any(), Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(new LinkData());
        when(linkDataRepository.getByChatIdLinkId(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(Optional.of(new LinkData()));
        when(linkMapper.createLinkResponse(Mockito.any(), Mockito.anyString()))
                .thenReturn(new LinkResponse(1, "string", List.of(), List.of()));

        linkDataService.trackLink(1, new AddLinkRequest("string", List.of(), List.of()));

        verify(tgChatService, times(1)).getByChatId(Mockito.anyLong());
        verify(linkRepository, times(1)).getByLink(Mockito.anyString());
        verify(linkMapper, times(1)).createLinkData(Mockito.any(), Mockito.anyLong(), Mockito.anyLong());
        verify(linkDataRepository, times(1)).getByChatIdLinkId(Mockito.anyLong(), Mockito.anyLong());
        verify(linkDataRepository, times(1)).update(Mockito.any());
        verify(linkMapper, times(1)).createLinkResponse(Mockito.any(), Mockito.anyString());
    }

    @Test
    public void untrackLinkTest() {
        when(tgChatService.getByChatId(Mockito.anyLong())).thenReturn(new TgChat(1L, 123));
        when(linkRepository.getByLink(Mockito.anyString()))
                .thenReturn(Optional.of(new Link(1L, "string", LocalDateTime.now())));
        when(linkDataRepository.getByChatIdLinkId(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(Optional.of(new LinkData()));
        when(linkMapper.createLinkResponse(Mockito.any(), Mockito.anyString()))
                .thenReturn(new LinkResponse(1, "string", List.of(), List.of()));

        linkDataService.untrackLink(1, new RemoveLinkRequest("string"));

        verify(tgChatService, times(1)).getByChatId(Mockito.anyLong());
        verify(linkRepository, times(1)).getByLink(Mockito.anyString());
        verify(linkDataRepository, times(1)).getByChatIdLinkId(Mockito.anyLong(), Mockito.anyLong());
        verify(linkDataRepository, times(1)).delete(Mockito.any());
        verify(linkMapper, times(1)).createLinkResponse(Mockito.any(), Mockito.anyString());
    }
}
