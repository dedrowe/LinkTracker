package backend.academy.bot.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import backend.academy.bot.service.apiClient.wrapper.ScrapperClientWrapper;
import backend.academy.shared.dto.LinkResponse;
import backend.academy.shared.dto.ListLinkResponse;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GetLinksCommandTest {

    @Mock
    private ScrapperClientWrapper client;

    @InjectMocks
    private GetLinksCommand commandExecutor;

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

    @Test
    public void emptyListTest() {
        String command = "/list";
        when(client.getLinks(anyLong())).thenReturn(new ListLinkResponse(List.of(), 0));
        when(message.text()).thenReturn(command);

        String expectedResult = "В данный момент никакие ссылки не отслеживаются";

        Optional<String> actualResult = commandExecutor.execute(update);

        assertThat(actualResult).isPresent();
        assertThat(actualResult.get()).isEqualTo(expectedResult);
    }

    @Test
    @SuppressWarnings("MisleadingEscapedSpace")
    public void notEmptyListTest() {
        String command = "/list";
        when(client.getLinks(anyLong()))
                .thenReturn(new ListLinkResponse(
                        List.of(
                                new LinkResponse(
                                        1L, "https://example.com", List.of("tag1", "tag2"), List.of("user=user1")),
                                new LinkResponse(1L, "https://example2.com", List.of("tag3", "tag4"), List.of())),
                        2));
        when(message.text()).thenReturn(command);

        String expectedResult =
                """
           Ссылка: https://example.com
           Тэги: tag1, tag2
           Фильтры: user=user1
           Ссылка: https://example2.com
           Тэги: tag3, tag4
           Фильтры:\s
           """;

        Optional<String> actualResult = commandExecutor.execute(update);

        assertThat(actualResult).isPresent();
        assertThat(actualResult.get()).isEqualTo(expectedResult);
    }

    @Test
    @SuppressWarnings("MisleadingEscapedSpace")
    public void tagListTest() {
        String command = "/list tag1";
        when(client.getLinksByTag(anyLong(), any()))
                .thenReturn(new ListLinkResponse(
                        List.of(
                                new LinkResponse(
                                        1L, "https://example.com", List.of("tag1", "tag2"), List.of("user=user1")),
                                new LinkResponse(1L, "https://example2.com", List.of("tag3", "tag4"), List.of())),
                        2));
        when(message.text()).thenReturn(command);

        String expectedResult =
                """
       Ссылки с тэгом tag1:
       Ссылка: https://example.com
       Тэги: tag1, tag2
       Фильтры: user=user1
       Ссылка: https://example2.com
       Тэги: tag3, tag4
       Фильтры:\s
       """;

        Optional<String> actualResult = commandExecutor.execute(update);

        assertThat(actualResult.get()).isEqualTo(expectedResult);
    }
}
