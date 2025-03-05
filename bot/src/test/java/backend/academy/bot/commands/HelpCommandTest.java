package backend.academy.bot.commands;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.bot.service.LinkMapper;
import backend.academy.bot.service.ScrapperClient;
import backend.academy.bot.stateStorage.TrackStateStorage;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(
        classes = {
            GetLinksCommand.class,
            HelpCommand.class,
            RegisterChatCommand.class,
            TrackLinkCommand.class,
            UntrackLinkCommand.class,
            ScrapperClient.class,
            TrackStateStorage.class,
            LinkMapper.class
        })
public class HelpCommandTest {

    @MockitoBean
    private ScrapperClient scrapperClient;

    @MockitoBean
    private TrackStateStorage trackStateStorage;

    @MockitoBean
    private LinkMapper linkMapper;

    @Autowired
    private HelpCommand commandExecutor;

    @Test
    public void commandsListTest() {
        String expectedResult =
                """
            Этот бот предназначен для отслеживания обновлений со ссылок различных сервисов.
            Ниже приведет список доступных команд.
            /list - Получение списка всех отслеживаемых ссылок
            /start - Регистрация чата (выполняется автоматически при создании чата с ботом)
            /track - Регистрация ссылки для отслеживания, синтаксис: /track <link>
            /untrack - Отмена отслеживания ссылки, синтаксис: /untrack <link>
            """;

        Optional<String> actualResult = commandExecutor.execute(null);

        assertThat(actualResult).isPresent();
        assertThat(actualResult.get()).isEqualTo(expectedResult);
    }
}
