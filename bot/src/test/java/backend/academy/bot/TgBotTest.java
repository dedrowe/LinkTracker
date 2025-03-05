package backend.academy.bot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.bot.commands.GetLinksCommand;
import backend.academy.bot.service.TgCommandsDispatcher;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TgBotTest {

    @Mock
    private Update update;

    @Mock
    private Message message;

    @Mock
    private Chat chat;

    @Mock
    private GetLinksCommand command;

    private final TgCommandsDispatcher dispatcher = mock(TgCommandsDispatcher.class);

    private final TelegramBot bot = mock(TelegramBot.class);

    private final TgBot tgBot = new TgBot(dispatcher, bot);

    private final long expectedChatId = 1L;

    @BeforeEach
    void setUp() {
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(expectedChatId);
    }

    @Test
    public void processValidCommandWithResponseTest() {
        String expectedResult = "example response";
        when(dispatcher.dispatchCommand(any())).thenReturn(Optional.of(command));
        when(command.execute(any())).thenReturn(Optional.of(expectedResult));
        TgBot.TgUpdatesListener listener = tgBot.new TgUpdatesListener();

        listener.process(List.of(update));

        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(bot, times(1)).execute(messageCaptor.capture(), any());
        assertThat(messageCaptor.getValue().getParameters().get("chat_id")).isEqualTo(expectedChatId);
        assertThat(messageCaptor.getValue().getParameters().get("text")).isEqualTo(expectedResult);
    }

    @Test
    public void processValidCommandWithoutResponseTest() {
        String expectedResult = "example response";
        when(dispatcher.dispatchCommand(any())).thenReturn(Optional.of(command));
        when(command.execute(any())).thenReturn(Optional.empty());
        TgBot.TgUpdatesListener listener = tgBot.new TgUpdatesListener();

        listener.process(List.of(update));

        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(bot, times(0)).execute(messageCaptor.capture(), any());
    }

    @Test
    public void processInvalidCommandTest() {
        String expectedResult = "Команда не найдена, для просмотра доступных команд введите /help";

        when(dispatcher.dispatchCommand(any())).thenReturn(Optional.empty());
        TgBot.TgUpdatesListener listener = tgBot.new TgUpdatesListener();

        listener.process(List.of(update));

        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(bot, times(1)).execute(messageCaptor.capture(), any());
        assertThat(messageCaptor.getValue().getParameters().get("chat_id")).isEqualTo(expectedChatId);
        assertThat(messageCaptor.getValue().getParameters().get("text")).isEqualTo(expectedResult);
    }
}
