package backend.academy.bot.commands;

import backend.academy.bot.exceptionHandling.exceptions.InvalidCommandSyntaxException;
import backend.academy.bot.service.ScrapperClient;
import backend.academy.shared.dto.RemoveLinkRequest;
import com.pengrad.telegrambot.model.Update;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component("/untrack")
@Slf4j
public class UntrackLinkCommand extends TgBotCommand {

    public UntrackLinkCommand(ScrapperClient client) {
        super(client, "Отмена отслеживания ссылки, синтаксис: /untrack <link>");
    }

    @Override
    public Optional<String> execute(Update update) {
        String[] command = update.message().text().split(" ");
        if (command.length != 2) {
            InvalidCommandSyntaxException ex =
                    new InvalidCommandSyntaxException("Неверный формат команды, ожидается: /untrack <link>");
            MDC.put("command", update.message().text());
            log.error("Неверный формат команды", ex);
            MDC.clear();
            throw ex;
        }
        client.untrackLink(update.message().chat().id(), new RemoveLinkRequest(command[1]));
        return Optional.of("Ссылка успешно удалена");
    }
}
