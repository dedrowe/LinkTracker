package backend.academy.bot.service;

import backend.academy.bot.commands.TgBotCommand;
import com.pengrad.telegrambot.model.Update;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class TgCommandsDispatcher {

    private final Map<String, TgBotCommand> commands;

    public Map<String, TgBotCommand> getCommands() {
        return commands;
    }

    public Optional<TgBotCommand> dispatchCommand(Update update) {
        String command = update.message().text().split(" ")[0];
        TgBotCommand tgBotCommand = commands.getOrDefault(command, null);
        if (tgBotCommand == null) {
            return Optional.empty();
        }
        return Optional.of(tgBotCommand);
    }
}
