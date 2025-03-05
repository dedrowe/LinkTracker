package backend.academy.scrapper.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.entity.TgChat;
import backend.academy.scrapper.exceptionHandling.exceptions.TgChatException;
import backend.academy.scrapper.repository.linkdata.LinkDataRepository;
import backend.academy.scrapper.repository.tgchat.TgChatRepository;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
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
        when(tgChatRepository.create(any())).thenReturn(CompletableFuture.completedFuture(null));

        tgChatService.registerTgChat(123);

        verify(tgChatRepository, times(1)).create(any());
    }

    @Test
    public void deleteSuccessTest() {
        when(tgChatRepository.getByChatId(Mockito.anyLong()))
                .thenReturn(CompletableFuture.completedFuture(Optional.of(new TgChat(1L, 123))));
        when(linkDataRepository.getByChatId(Mockito.anyLong()))
                .thenReturn(CompletableFuture.completedFuture(List.of(new LinkData(), new LinkData())));
        when(tgChatRepository.delete(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(linkDataRepository.delete(any())).thenReturn(CompletableFuture.completedFuture(null));

        tgChatService.deleteTgChat(123);

        verify(tgChatRepository, times(1)).getByChatId(Mockito.anyLong());
        verify(linkDataRepository, times(1)).getByChatId(Mockito.anyLong());
        verify(linkDataRepository, times(2)).delete(any());
        verify(tgChatRepository, times(1)).delete(any());
    }

    @Test
    public void deleteFailTest() {
        when(tgChatRepository.getByChatId(Mockito.anyLong()))
                .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        assertThatThrownBy(() -> tgChatService.deleteTgChat(123)).isInstanceOf(TgChatException.class);
        verify(tgChatRepository, times(1)).getByChatId(Mockito.anyLong());
    }

    @Test
    public void getByIdSuccessTest() {
        TgChat tgChat = new TgChat(1L, 123);
        when(tgChatRepository.getById(Mockito.anyLong()))
                .thenReturn(CompletableFuture.completedFuture(Optional.of(tgChat)));

        TgChat actualChat = tgChatService.getById(123);

        assertThat(actualChat.chatId()).isEqualTo(tgChat.chatId());
        assertThat(actualChat.id()).isEqualTo(tgChat.id());
        verify(tgChatRepository, times(1)).getById(Mockito.anyLong());
    }

    @Test
    public void getByIdFailTest() {
        when(tgChatRepository.getById(Mockito.anyLong()))
                .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        assertThatThrownBy(() -> tgChatService.getById(123)).isInstanceOf(TgChatException.class);
        verify(tgChatRepository, times(1)).getById(Mockito.anyLong());
    }

    @Test
    public void getByChatIdSuccessTest() {
        TgChat tgChat = new TgChat(1L, 123);
        when(tgChatRepository.getByChatId(Mockito.anyLong()))
                .thenReturn(CompletableFuture.completedFuture(Optional.of(tgChat)));

        TgChat actualChat = tgChatService.getByChatId(123);

        assertThat(actualChat.chatId()).isEqualTo(tgChat.chatId());
        assertThat(actualChat.id()).isEqualTo(tgChat.id());
        verify(tgChatRepository, times(1)).getByChatId(Mockito.anyLong());
    }

    @Test
    public void getByChatIdFailTest() {
        when(tgChatRepository.getByChatId(Mockito.anyLong()))
                .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        assertThatThrownBy(() -> tgChatService.getByChatId(123)).isInstanceOf(TgChatException.class);
        verify(tgChatRepository, times(1)).getByChatId(Mockito.anyLong());
    }
}
