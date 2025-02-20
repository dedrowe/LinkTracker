package backend.academy.bot.commands;

import backend.academy.bot.service.ScrapperClient;
import com.pengrad.telegrambot.model.Update;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component("/start")
public class RegisterChatCommand extends TgBotCommand {

    public RegisterChatCommand(ScrapperClient client) {
        super(client, "Регистрация чата (выполняется автоматически при создании чата с ботом)");
    }

    @Override
    public Optional<String> execute(Update update) {
        client.registerChat(update.message().chat().id());
        return Optional.empty();
    }
}
