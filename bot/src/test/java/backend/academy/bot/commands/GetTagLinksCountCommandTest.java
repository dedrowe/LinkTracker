package backend.academy.bot.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import backend.academy.bot.service.ScrapperClient;
import backend.academy.shared.dto.ListTagLinkCount;
import backend.academy.shared.dto.TagLinkCount;
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
public class GetTagLinksCountCommandTest {

    @Mock
    private ScrapperClient client;

    @InjectMocks
    private GetTagLinksCountCommand commandExecutor;

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
    public void emptyTagLinksCountTest() {
        when(client.getTagLinksCount(anyLong())).thenReturn(new ListTagLinkCount(List.of()));

        String expectedResult = "В данный момент никакие тэги не указаны";

        Optional<String> actualResult = commandExecutor.execute(update);

        assertThat(actualResult.get()).isEqualTo(expectedResult);
    }

    @Test
    public void TagLinksCountTest() {
        when(client.getTagLinksCount(anyLong()))
                .thenReturn(new ListTagLinkCount(List.of(new TagLinkCount("tag1", 2), new TagLinkCount("tag2", 1))));

        String expectedResult =
                """
            Тэг: tag1\tКоличество ссылок: 2
            Тэг: tag2\tКоличество ссылок: 1
            """;

        Optional<String> actualResult = commandExecutor.execute(update);

        assertThat(actualResult.get()).isEqualTo(expectedResult);
    }
}
