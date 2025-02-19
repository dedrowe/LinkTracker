package backend.academy.bot.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import java.util.List;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TgUpdatesListener implements UpdatesListener {

    private final TelegramBot bot;

    @Override
    public int process(List<Update> list) {
        return CONFIRMED_UPDATES_ALL;
    }
}
