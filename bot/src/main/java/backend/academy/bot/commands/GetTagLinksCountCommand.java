package backend.academy.bot.commands;

import backend.academy.bot.service.apiClient.wrapper.ScrapperClientWrapper;
import backend.academy.shared.dto.ListTagLinkCount;
import backend.academy.shared.dto.TagLinkCount;
import com.pengrad.telegrambot.model.Update;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component("/tags")
public class GetTagLinksCountCommand extends TgBotCommand {

    public GetTagLinksCountCommand(ScrapperClientWrapper client) {
        super(client, "Получение списка тэгов и количества отслеживаемых ссылок с этими тэгами");
    }

    @Override
    public Optional<String> execute(Update update) {
        ListTagLinkCount tags = client.getTagLinksCount(update.message().chat().id());
        if (tags.tags().isEmpty()) {
            return Optional.of("В данный момент никакие тэги не указаны");
        }
        StringBuilder sb = new StringBuilder();
        for (TagLinkCount tag : tags.tags()) {
            sb.append("Тэг: ")
                    .append(tag.tag())
                    .append("\t")
                    .append("Количество ссылок: ")
                    .append(tag.linksCount())
                    .append("\n");
        }
        return Optional.of(sb.toString());
    }
}
