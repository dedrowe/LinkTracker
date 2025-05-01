package backend.academy.bot.commands;

import backend.academy.bot.service.apiClient.wrapper.ScrapperClientWrapper;
import com.pengrad.telegrambot.model.Update;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public abstract class TgBotCommand {

    protected final ScrapperClientWrapper client;

    @Getter
    protected final String description;

    public abstract Optional<String> execute(Update update);
}
