package backend.academy.bot.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.bot.dto.LinkState;
import backend.academy.bot.exceptionHandling.exceptions.InvalidCommandSyntaxException;
import backend.academy.bot.service.LinkMapper;
import backend.academy.bot.service.apiClient.wrapper.ScrapperClientWrapper;
import backend.academy.bot.stateStorage.TrackStateStorage;
import backend.academy.bot.stateStorage.state.LinkTrackState;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("UnusedVariable")
public class TrackLinkCommandTest {

    @Mock
    private ScrapperClientWrapper client;

    @Mock
    private TrackStateStorage trackStateStorage;

    @Mock
    private LinkMapper linkMapper;

    @InjectMocks
    private TrackLinkCommand commandExecutor;

    @Mock
    private Update update;

    @Mock
    private Message message;

    @Mock
    private Chat chat;

    private long chatId = 1L;

    @BeforeEach
    public void setUp() {
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
    }

    @Test
    public void setLinkTest() {
        String command = "/track https://example.com";
        LinkState expectedState = new LinkState();
        expectedState.link("https://example.com");
        expectedState.state(LinkTrackState.TAGS);
        String expectedResult = "Введите через пробел тэги для ссылки, введите /skip для пропуска";

        when(message.text()).thenReturn(command);

        Optional<String> actualResult = commandExecutor.execute(update);

        verify(trackStateStorage).put(chatId, expectedState);
        assertThat(actualResult.get()).isEqualTo(expectedResult);
    }

    @Test
    public void setLinkInvalidCommandTest() {
        String command = "/track";

        when(message.text()).thenReturn(command);

        assertThatThrownBy(() -> commandExecutor.execute(update)).isInstanceOf(InvalidCommandSyntaxException.class);
        assertThatThrownBy(() -> commandExecutor.execute(update))
                .hasMessage("Неверный формат команды, ожидается: /track <link>");
    }

    @Test
    public void setTagsTest() {
        String command = "tag1 tag2";
        List<String> tags = List.of("tag1", "tag2");
        LinkState state = new LinkState();
        state.state(LinkTrackState.TAGS);
        String expectedResult =
                "Введите через пробел фильтры для ссылки в формате key:value, введите /skip для пропуска";

        when(message.text()).thenReturn(command);
        when(trackStateStorage.containsState(anyLong())).thenReturn(true);
        when(trackStateStorage.getState(anyLong())).thenReturn(state);

        Optional<String> actualResult = commandExecutor.execute(update);

        assertThat(state.state()).isEqualTo(LinkTrackState.FILTERS);
        assertThat(state.tags()).isEqualTo(tags);
        assertThat(actualResult.get()).isEqualTo(expectedResult);
    }

    @Test
    public void setEmptyTagsTest() {
        String command = "/skip";
        LinkState state = new LinkState();
        state.state(LinkTrackState.TAGS);
        String expectedResult =
                "Введите через пробел фильтры для ссылки в формате key:value, введите /skip для пропуска";

        when(message.text()).thenReturn(command);
        when(trackStateStorage.containsState(anyLong())).thenReturn(true);
        when(trackStateStorage.getState(anyLong())).thenReturn(state);

        Optional<String> actualResult = commandExecutor.execute(update);

        assertThat(state.state()).isEqualTo(LinkTrackState.FILTERS);
        assertThat(state.tags()).isEqualTo(List.of());
        assertThat(actualResult.get()).isEqualTo(expectedResult);
    }

    @Test
    public void setFiltersTest() {
        String command = "key:value";
        List<String> filters = List.of("key:value");
        LinkState state = new LinkState();
        state.state(LinkTrackState.FILTERS);
        String expectedResult = "Ссылка успешно добавлена";

        when(message.text()).thenReturn(command);
        when(trackStateStorage.containsState(anyLong())).thenReturn(true);
        when(trackStateStorage.getState(anyLong())).thenReturn(state);

        Optional<String> actualResult = commandExecutor.execute(update);

        assertThat(state.filters()).isEqualTo(filters);
        verify(trackStateStorage, times(1)).remove(chatId);
        verify(client, times(1)).trackLink(anyLong(), any());
        assertThat(actualResult.get()).isEqualTo(expectedResult);
    }

    @Test
    public void setEmptyFiltersTest() {
        String command = "/skip";
        LinkState state = new LinkState();
        state.state(LinkTrackState.FILTERS);
        String expectedResult = "Ссылка успешно добавлена";

        when(message.text()).thenReturn(command);
        when(trackStateStorage.containsState(anyLong())).thenReturn(true);
        when(trackStateStorage.getState(anyLong())).thenReturn(state);

        Optional<String> actualResult = commandExecutor.execute(update);

        assertThat(state.filters()).isEqualTo(List.of());
        verify(trackStateStorage, times(1)).remove(chatId);
        verify(client, times(1)).trackLink(anyLong(), any());
        assertThat(actualResult.get()).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @ValueSource(strings = {":value", "key:", "key:value:options", ":"})
    public void setFiltersInvalidCommandTest(String command) {
        LinkState state = new LinkState();
        state.state(LinkTrackState.FILTERS);

        when(message.text()).thenReturn(command);
        when(trackStateStorage.containsState(anyLong())).thenReturn(true);
        when(trackStateStorage.getState(anyLong())).thenReturn(state);

        assertThatThrownBy(() -> commandExecutor.execute(update)).isInstanceOf(InvalidCommandSyntaxException.class);
        assertThatThrownBy(() -> commandExecutor.execute(update))
                .hasMessage("Фильтры должны указываться в формате key:value");
    }
}
