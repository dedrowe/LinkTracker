package backend.academy.bot.commands;

import backend.academy.bot.service.ScrapperClient;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RegisterChatCommandTest {

    @Mock
    private ScrapperClient client;

    @InjectMocks
    private RegisterChatCommand commandExecutor;

    @Mock
    private Update update;

    @Mock
    private Message message;

    @Mock
    private Chat chat;

    @BeforeEach
    public void setUp() {
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "/start",
        "/start 123123"
    })
    public void validCommandParseTest(String command) {

        assertThat(commandExecutor.execute(update)).isEmpty();
    }
}
