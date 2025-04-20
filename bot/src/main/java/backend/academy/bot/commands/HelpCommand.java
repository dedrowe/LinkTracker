package backend.academy.bot.commands;

import backend.academy.bot.service.apiClient.wrapper.ScrapperClientWrapper;
import com.pengrad.telegrambot.model.Update;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component("/help")
public class HelpCommand extends TgBotCommand {

    private final Map<String, TgBotCommand> commands;

    public HelpCommand(ScrapperClientWrapper client, Map<String, TgBotCommand> commands) {
        super(client, "");
        this.commands = commands;
    }

    @Override
    public Optional<String> execute(Update update) {
        StringBuilder sb = new StringBuilder();
        sb.append("Этот бот предназначен для отслеживания обновлений со ссылок различных сервисов.")
                .append('\n')
                .append("Ниже приведет список доступных команд.")
                .append('\n');
        for (Map.Entry<String, TgBotCommand> entry : commands.entrySet()) {
            sb.append(entry.getKey())
                    .append(" - ")
                    .append(entry.getValue().description)
                    .append('\n');
        }
        return Optional.of(sb.toString());
    }
}
