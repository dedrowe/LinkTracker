package backend.academy.scrapper.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.entity.TgChat;
import backend.academy.scrapper.exceptionHandling.exceptions.NotFoundException;
import backend.academy.scrapper.repository.linkdata.LinkDataRepository;
import backend.academy.scrapper.repository.tgchat.TgChatRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TgChatServiceTest {

    private final TgChatRepository tgChatRepository = mock(TgChatRepository.class);

    private final LinkDataRepository linkDataRepository = mock(LinkDataRepository.class);

    private final TgChatService tgChatService = new TgChatService(tgChatRepository, linkDataRepository);

    @Test
    public void registerTest() {
        tgChatService.registerTgChat(123);

        verify(tgChatRepository, times(1)).create(Mockito.any());
    }

    @Test
    public void deleteSuccessTest() {
        when(tgChatRepository.getByChatId(Mockito.anyLong())).thenReturn(Optional.of(new TgChat(1L, 123)));
        when(linkDataRepository.getByChatId(Mockito.anyLong())).thenReturn(List.of(new LinkData(), new LinkData()));

        tgChatService.deleteTgChat(123);

        verify(tgChatRepository, times(1)).getByChatId(Mockito.anyLong());
        verify(linkDataRepository, times(1)).getByChatId(Mockito.anyLong());
        verify(linkDataRepository, times(2)).delete(Mockito.any());
        verify(tgChatRepository, times(1)).delete(Mockito.any());
    }

    @Test
    public void deleteFailTest() {
        when(tgChatRepository.getByChatId(Mockito.anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tgChatService.deleteTgChat(123)).isInstanceOf(NotFoundException.class);
        verify(tgChatRepository, times(1)).getByChatId(Mockito.anyLong());
    }

    @Test
    public void getByIdSuccessTest() {
        TgChat tgChat = new TgChat(1L, 123);
        when(tgChatRepository.getById(Mockito.anyLong())).thenReturn(Optional.of(tgChat));

        TgChat actualChat = tgChatService.getById(123);

        assertThat(actualChat.chatId()).isEqualTo(tgChat.chatId());
        assertThat(actualChat.id()).isEqualTo(tgChat.id());
        verify(tgChatRepository, times(1)).getById(Mockito.anyLong());
    }

    @Test
    public void getByIdFailTest() {
        when(tgChatRepository.getById(Mockito.anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tgChatService.getById(123)).isInstanceOf(NotFoundException.class);
        verify(tgChatRepository, times(1)).getById(Mockito.anyLong());
    }

    @Test
    public void getByChatIdSuccessTest() {
        TgChat tgChat = new TgChat(1L, 123);
        when(tgChatRepository.getByChatId(Mockito.anyLong())).thenReturn(Optional.of(tgChat));

        TgChat actualChat = tgChatService.getByChatId(123);

        assertThat(actualChat.chatId()).isEqualTo(tgChat.chatId());
        assertThat(actualChat.id()).isEqualTo(tgChat.id());
        verify(tgChatRepository, times(1)).getByChatId(Mockito.anyLong());
    }

    @Test
    public void getByChatIdFailTest() {
        when(tgChatRepository.getByChatId(Mockito.anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tgChatService.getByChatId(123)).isInstanceOf(NotFoundException.class);
        verify(tgChatRepository, times(1)).getByChatId(Mockito.anyLong());
    }
}
