package backend.academy.bot.commands;

import backend.academy.bot.exceptionHandling.exceptions.InvalidCommandSyntaxException;
import backend.academy.bot.service.ScrapperClient;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UntrackLinkCommandTest {

    @Mock
    private ScrapperClient client;

    @InjectMocks
    private UntrackLinkCommand commandExecutor;

    @Mock
    private Update update;

    @Mock
    private Message message;

    @Mock
    private Chat chat;

    @BeforeEach
    public void setUp() {
        when(update.message()).thenReturn(message);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "/untrack https://example.com",
        "/untrack 123123"
    })
    public void validCommandParseTest(String command) {
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
        when(update.message().text()).thenReturn(command);

        String expectedMessage = "Ссылка успешно удалена";

        Optional<String> actualMessage = commandExecutor.execute(update);

        assertThat(actualMessage).isPresent();
        assertThat(actualMessage.get()).isEqualTo(expectedMessage);
    }

    @Test
    public void invalidCommandParseTest() {
        when(update.message().text()).thenReturn("/untrack");

        assertThatThrownBy(() -> commandExecutor.execute(update)).isInstanceOf(InvalidCommandSyntaxException.class);
    }
}
