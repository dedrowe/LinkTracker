package backend.academy.bot.commands;

import backend.academy.bot.exceptionHandling.exceptions.InvalidCommandSyntaxException;
import backend.academy.bot.service.ScrapperClient;
import backend.academy.shared.dto.RemoveLinkRequest;
import com.pengrad.telegrambot.model.Update;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component("/untrack")
@Slf4j
public class UntrackLinkCommand extends TgBotCommand {

    public UntrackLinkCommand(ScrapperClient client) {
        super(client, "Отмена отслеживания ссылки, синтаксис: /untrack <link>");
    }

    @Override
    @SuppressWarnings("PMD.UnusedLocalVariable")
    public Optional<String> execute(Update update) {
        String[] command = update.message().text().split(" ");
        if (command.length != 2) {
            throw new InvalidCommandSyntaxException(
                    "Неверный формат команды, ожидается: /untrack <link>",
                    update.message().text());
        }
        client.untrackLink(update.message().chat().id(), new RemoveLinkRequest(command[1]));
        return Optional.of("Ссылка успешно удалена");
    }
}
