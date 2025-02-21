package backend.academy.bot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import backend.academy.bot.commands.GetLinksCommand;
import backend.academy.bot.commands.HelpCommand;
import backend.academy.bot.commands.RegisterChatCommand;
import backend.academy.bot.commands.TgBotCommand;
import backend.academy.bot.commands.TrackLinkCommand;
import backend.academy.bot.commands.UntrackLinkCommand;
import backend.academy.bot.stateStorage.TrackStateStorage;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(
        classes = {
            GetLinksCommand.class,
            HelpCommand.class,
            RegisterChatCommand.class,
            UntrackLinkCommand.class,
            TrackLinkCommand.class,
            TrackStateStorage.class,
            TgCommandsDispatcher.class
        })
public class TgCommandsDispatcherTest {

    @MockitoBean
    private GetLinksCommand getLinksCommand;

    @MockitoBean
    private HelpCommand helpCommand;

    @MockitoBean
    private RegisterChatCommand registerChatCommand;

    @MockitoBean
    private TrackLinkCommand trackLinkCommand;

    @MockitoBean
    private UntrackLinkCommand untrackLinkCommand;

    @MockitoBean
    private TrackStateStorage trackStateStorage;

    @Mock
    private Update update;

    @Mock
    private Message message;

    @Mock
    private Chat chat;

    @Autowired
    private TgCommandsDispatcher tgCommandsDispatcher;

    @BeforeEach
    public void setUp() {
        when(update.message()).thenReturn(message);
    }

    @Test
    public void getLinksCommandTest() {
        when(message.text()).thenReturn("/list");

        Optional<TgBotCommand> actualCommand = tgCommandsDispatcher.dispatchCommand(update);

        assertThat(actualCommand).isPresent();
        assertThat(actualCommand.get()).isInstanceOf(GetLinksCommand.class);
    }

    @Test
    public void helpCommandTest() {
        when(message.text()).thenReturn("/help");

        Optional<TgBotCommand> actualCommand = tgCommandsDispatcher.dispatchCommand(update);

        assertThat(actualCommand).isPresent();
        assertThat(actualCommand.get()).isInstanceOf(HelpCommand.class);
    }

    @Test
    public void registerChatCommandTest() {
        when(message.text()).thenReturn("/start");

        Optional<TgBotCommand> actualCommand = tgCommandsDispatcher.dispatchCommand(update);

        assertThat(actualCommand).isPresent();
        assertThat(actualCommand.get()).isInstanceOf(RegisterChatCommand.class);
    }

    @Test
    public void trackLinkCommandTest() {
        when(message.text()).thenReturn("/track");

        Optional<TgBotCommand> actualCommand = tgCommandsDispatcher.dispatchCommand(update);

        assertThat(actualCommand).isPresent();
        assertThat(actualCommand.get()).isInstanceOf(TrackLinkCommand.class);
    }

    @Test
    public void trackLinkWithStateTest() {
        when(message.text()).thenReturn("tag1 tag2");
        when(message.chat()).thenReturn(chat);
        when(trackStateStorage.containsState(anyLong())).thenReturn(true);

        Optional<TgBotCommand> actualCommand = tgCommandsDispatcher.dispatchCommand(update);

        assertThat(actualCommand).isPresent();
        assertThat(actualCommand.get()).isInstanceOf(TrackLinkCommand.class);
    }

    @Test
    public void untrackLinkCommandTest() {
        when(message.text()).thenReturn("/untrack");

        Optional<TgBotCommand> actualCommand = tgCommandsDispatcher.dispatchCommand(update);

        assertThat(actualCommand).isPresent();
        assertThat(actualCommand.get()).isInstanceOf(UntrackLinkCommand.class);
    }

    @Test
    public void wrongCommandTest() {
        when(message.text()).thenReturn("/wrongCommand");
        when(message.chat()).thenReturn(chat);

        Optional<TgBotCommand> actualCommand = tgCommandsDispatcher.dispatchCommand(update);

        assertThat(actualCommand).isEmpty();
    }
}
