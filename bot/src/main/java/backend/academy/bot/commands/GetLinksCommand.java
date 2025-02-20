package backend.academy.bot.commands;

import backend.academy.bot.service.ScrapperClient;
import backend.academy.shared.dto.LinkResponse;
import backend.academy.shared.dto.ListLinkResponse;
import com.pengrad.telegrambot.model.Update;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component("/list")
public class GetLinksCommand extends TgBotCommand {

    public GetLinksCommand(ScrapperClient client) {
        super(client, "Получение списка всех отслеживаемых ссылок");
    }

    @Override
    public Optional<String> execute(Update update) {
        ListLinkResponse response = client.getLinks(update.message().chat().id());
        if (response.links().isEmpty()) {
            return Optional.of("В данный момент никакие ссылки не отслеживаются");
        }
        StringBuilder sb = new StringBuilder();
        for (LinkResponse link : response.links()) {
            sb.append("Ссылка: ").append(link.url()).append('\n').append("Тэги: ");
            for (String tag : link.tags()) {
                sb.append(tag).append('\n');
            }
            if (link.tags().isEmpty()) {
                sb.append('\n');
            }
            sb.append("Фильтры: ");
            for (String filter : link.filters()) {
                sb.append(filter).append('\n');
            }
            if (link.filters().isEmpty()) {
                sb.append('\n');
            }
        }
        return Optional.of(sb.toString());
    }
}
