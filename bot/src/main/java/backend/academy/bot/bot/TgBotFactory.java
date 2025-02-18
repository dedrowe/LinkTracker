package backend.academy.bot.bot;

import backend.academy.bot.BotConfig;
import com.pengrad.telegrambot.TelegramBot;
import lombok.Getter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Getter
@Service
@Scope("singleton")
public class TgBotFactory {

    private final TelegramBot bot;

    public TgBotFactory(BotConfig config) {
        bot = new TelegramBot(config.telegramToken());
        bot.setUpdatesListener(new TgUpdatesListener(bot));
    }
}
