package backend.academy.bot.commands;

import backend.academy.bot.service.apiClient.wrapper.ScrapperClientWrapper;
import com.pengrad.telegrambot.model.Update;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component("/start")
public class RegisterChatCommand extends TgBotCommand {

    public RegisterChatCommand(ScrapperClientWrapper client) {
        super(client, "Регистрация чата (выполняется автоматически при создании чата с ботом)");
    }

    @Override
    public Optional<String> execute(Update update) {
        client.registerChat(update.message().chat().id());
        return Optional.of(
                """
            Привет! Этот бот предоставляет удобный способ оповещения об обновлениях на зарегистрированных ссылках.
            Для добавления ссылки введите команду /track <ссылка>
            Для вывода всех доступных команд введите /help
            """);
    }
}
