package backend.academy.bot.service;

import backend.academy.bot.commands.GetLinksCommand;
import backend.academy.bot.commands.HelpCommand;
import backend.academy.bot.commands.RegisterChatCommand;
import backend.academy.bot.commands.TgBotCommand;
import backend.academy.bot.commands.TrackLinkCommand;
import backend.academy.bot.commands.UntrackLinkCommand;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {
    GetLinksCommand.class,
    HelpCommand.class,
    RegisterChatCommand.class,
    UntrackLinkCommand.class,
    TrackLinkCommand.class,
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

    @Mock
    private Update update;

    @Mock
    private Message message;

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
    public void untrackLinkCommandTest() {
        when(message.text()).thenReturn("/untrack");

        Optional<TgBotCommand> actualCommand = tgCommandsDispatcher.dispatchCommand(update);

        assertThat(actualCommand).isPresent();
        assertThat(actualCommand.get()).isInstanceOf(UntrackLinkCommand.class);
    }

    @Test
    public void wrongCommandTest() {
        when(message.text()).thenReturn("/wrnogCommand");

        Optional<TgBotCommand> actualCommand = tgCommandsDispatcher.dispatchCommand(update);

        assertThat(actualCommand).isEmpty();
    }
}
