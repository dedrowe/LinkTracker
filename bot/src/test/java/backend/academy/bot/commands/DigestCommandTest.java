package backend.academy.bot.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.bot.exceptionHandling.exceptions.InvalidCommandSyntaxException;
import backend.academy.bot.service.apiClient.ScrapperClient;
import backend.academy.bot.service.apiClient.wrapper.ScrapperClientWrapper;
import backend.academy.shared.dto.TgChatUpdateDto;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DigestCommandTest {

    @Mock
    private ScrapperClientWrapper client;

    @InjectMocks
    private DigestCommand commandExecutor;

    @Mock
    private Update update;

    @Mock
    private Message message;

    @Mock
    private Chat chat;

    @Test
    public void DigestOffTest() {
        String command = "/digest off";
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
        when(update.message().text()).thenReturn(command);

        String expectedResult = "Дайджест изменен";

        Optional<String> actualResult = commandExecutor.execute(update);

        verify(client, times(1)).updateChat(1L, new TgChatUpdateDto(null));
        assertThat(actualResult.get()).isEqualTo(expectedResult);
    }

    @Test
    public void DigestSetTest() {
        String command = "/digest 10:00";
        LocalTime expectedTime = LocalTime.of(10, 0);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
        when(update.message().text()).thenReturn(command);

        String expectedResult = "Дайджест изменен";

        Optional<String> actualResult = commandExecutor.execute(update);

        verify(client, times(1)).updateChat(1L, new TgChatUpdateDto(expectedTime));
        assertThat(actualResult.get()).isEqualTo(expectedResult);
    }

    @Test
    public void InvalidCommandTest() {
        String command = "/digest";
        when(update.message()).thenReturn(message);
        when(update.message().text()).thenReturn(command);

        assertThatThrownBy(() -> commandExecutor.execute(update)).isInstanceOf(InvalidCommandSyntaxException.class);
    }

    @Test
    public void InvalidTimeFormatTest() {
        String command = "/digest 10.00";
        when(update.message()).thenReturn(message);
        when(update.message().text()).thenReturn(command);

        assertThatThrownBy(() -> commandExecutor.execute(update)).isInstanceOf(InvalidCommandSyntaxException.class);
    }
}
