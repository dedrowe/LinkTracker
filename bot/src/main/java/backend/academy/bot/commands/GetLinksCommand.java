package backend.academy.bot.commands;

import backend.academy.bot.exceptionHandling.exceptions.InvalidCommandSyntaxException;
import backend.academy.bot.service.apiClient.wrapper.ScrapperClientWrapper;
import backend.academy.shared.dto.LinkResponse;
import backend.academy.shared.dto.ListLinkResponse;
import com.pengrad.telegrambot.model.Update;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component("/list")
public class GetLinksCommand extends TgBotCommand {

    public GetLinksCommand(ScrapperClientWrapper client) {
        super(
                client,
                "Получение списка всех отслеживаемых ссылок, дополнительно можно указать тэг, "
                        + "чтобы получить все ссылки с этим тэгом");
    }

    @Override
    @SuppressWarnings("StringSplitter")
    public Optional<String> execute(Update update) {
        String[] command = update.message().text().split(" ");
        StringBuilder sb = new StringBuilder();
        ListLinkResponse response;
        if (command.length == 1) {
            response = client.getLinks(update.message().chat().id());
        } else if (command.length == 2) {
            response = client.getLinksByTag(update.message().chat().id(), command[1]);
            sb.append("Ссылки с тэгом ").append(command[1]).append(":\n");
        } else {
            throw new InvalidCommandSyntaxException(
                    "Неверный формат команды, ожидается: /list или /list <tag>",
                    update.message().text());
        }
        if (response.links().isEmpty()) {
            return Optional.of("В данный момент никакие ссылки не отслеживаются");
        }
        for (LinkResponse link : response.links()) {
            sb.append("Ссылка: ").append(link.url()).append('\n').append("Тэги: ");
            for (int i = 0; i < link.tags().size(); ++i) {
                sb.append(link.tags().get(i));
                if (i < link.tags().size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append('\n');
            sb.append("Фильтры: ");
            for (int i = 0; i < link.filters().size(); ++i) {
                sb.append(link.filters().get(i));
                if (i < link.filters().size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append('\n');
        }
        return Optional.of(sb.toString());
    }
}
