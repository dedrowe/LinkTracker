package backend.academy.bot.commands;

import backend.academy.bot.exceptionHandling.exceptions.InvalidCommandSyntaxException;
import backend.academy.bot.service.ScrapperClient;
import backend.academy.shared.dto.AddLinkRequest;
import com.pengrad.telegrambot.model.Update;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component("/track")
public class TrackLinkCommand extends TgBotCommand {

    public TrackLinkCommand(ScrapperClient client) {
        super(client, "Регистрация ссылки для отслеживания, синтаксис: /track <link>");
    }

    @Override
    public Optional<String> execute(Update update) {
        String[] command = update.message().text().split(" ");
        if (command.length != 2) {
            throw new InvalidCommandSyntaxException("Неверный формат команды, ожидается: /track <link>");
        }
        client.trackLink(update.message().chat().id(), new AddLinkRequest(command[1], List.of(), List.of()));
        return Optional.of("Ссылка успешно добавлена");
    }
}
