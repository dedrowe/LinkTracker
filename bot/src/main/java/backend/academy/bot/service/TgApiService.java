package backend.academy.bot.service;

import backend.academy.bot.bot.TgBotFactory;
import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
@Slf4j
public class TgApiService {

    private final TelegramBot bot;

    @Autowired
    public TgApiService(TgBotFactory factory) {
        this.bot = factory.bot();
    }

    public void sendMessage(long chatId, String message) {
        SendMessage sendMessage = new SendMessage(chatId, message);
        bot.execute(sendMessage, new Callback<SendMessage, SendResponse>() {
            @Override
            public void onResponse(SendMessage sendMessage, SendResponse sendResponse) {
                if (!sendResponse.isOk()) {
                    log.error(sendResponse.description());
                }
            }
            @Override
            public void onFailure(SendMessage sendMessage, IOException e) {
                log.error(e.getMessage());
            }
        });
    }
}
