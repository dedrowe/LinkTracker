package backend.academy.bot.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import lombok.AllArgsConstructor;
import java.util.List;

@AllArgsConstructor
public class TgUpdatesListener implements UpdatesListener {

    private final TelegramBot bot;

    @Override
    public int process(List<Update> list) {
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
