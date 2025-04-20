package backend.academy.bot.commands;

import backend.academy.bot.dto.LinkState;
import backend.academy.bot.exceptionHandling.exceptions.InvalidCommandSyntaxException;
import backend.academy.bot.service.LinkMapper;
import backend.academy.bot.service.apiClient.wrapper.ScrapperClientWrapper;
import backend.academy.bot.stateStorage.TrackStateStorage;
import backend.academy.bot.stateStorage.state.LinkTrackState;
import com.pengrad.telegrambot.model.Update;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component("/track")
@Slf4j
public class TrackLinkCommand extends TgBotCommand {

    private final TrackStateStorage storage;

    private final LinkMapper linkMapper;

    public TrackLinkCommand(ScrapperClientWrapper client, TrackStateStorage storage, LinkMapper linkMapper) {
        super(client, "Регистрация ссылки для отслеживания, синтаксис: /track <link>");
        this.storage = storage;
        this.linkMapper = linkMapper;
    }

    @Override
    public Optional<String> execute(Update update) {
        if (!storage.containsState(update.message().chat().id())) {
            return setLink(update);
        }
        LinkState linkState = storage.getState(update.message().chat().id());
        return switch (linkState.state()) {
            case TAGS -> setTags(update, linkState);
            case FILTERS -> setFilters(update, linkState);
        };
    }

    private Optional<String> setLink(Update update) {
        String[] command = update.message().text().split(" ");
        if (command.length != 2) {
            throw new InvalidCommandSyntaxException(
                    "Неверный формат команды, ожидается: /track <link>",
                    update.message().text());
        }
        LinkState linkState = new LinkState();
        linkState.link(command[1]);
        linkState.state(LinkTrackState.TAGS);
        storage.put(update.message().chat().id(), linkState);
        return Optional.of("Введите через пробел тэги для ссылки, введите /skip для пропуска");
    }

    private Optional<String> setTags(Update update, LinkState linkState) {
        String[] command = update.message().text().split(" ");
        if (!command[0].equals("/skip")) {
            List<String> tags = List.of(command);
            linkState.tags(tags);
        } else {
            linkState.tags(List.of());
        }
        linkState.state(LinkTrackState.FILTERS);
        return Optional.of("Введите через пробел фильтры для ссылки в формате key:value, введите /skip для пропуска");
    }

    private Optional<String> setFilters(Update update, LinkState linkState) {
        String[] command = update.message().text().split(" ");
        if (!command[0].equals("/skip")) {
            for (String filter : command) {
                String[] tokens = filter.split(":");
                if (tokens.length != 2 || tokens[0].isEmpty() || tokens[1].isEmpty()) {
                    throw new InvalidCommandSyntaxException(
                            "Фильтры должны указываться в формате key:value",
                            update.message().text());
                }
            }
            List<String> filters = List.of(command);
            linkState.filters(filters);
        } else {
            linkState.filters(List.of());
        }
        storage.remove(update.message().chat().id());
        client.trackLink(update.message().chat().id(), linkMapper.createAddLinkRequest(linkState));
        return Optional.of("Ссылка успешно добавлена");
    }
}
