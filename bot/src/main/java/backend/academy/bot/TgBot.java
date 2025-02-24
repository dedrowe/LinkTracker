package backend.academy.bot;

import backend.academy.bot.exceptionHandling.exceptions.InvalidCommandSyntaxException;
import backend.academy.bot.service.TgCommandsDispatcher;
import backend.academy.shared.exceptions.ApiCallException;
import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.SendResponse;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TgBot {

    private final TgCommandsDispatcher dispatcher;

    private final TelegramBot bot;

    public TgBot(TgCommandsDispatcher dispatcher, BotConfig config) {
        this.dispatcher = dispatcher;
        this.bot = new TelegramBot(config.telegram().token());
        bot.setUpdatesListener(new TgUpdatesListener());
        setCommands();
    }

    public void sendMessage(long chatId, String message) {
        SendMessage sendMessage = new SendMessage(chatId, message);
        bot.execute(sendMessage, new Callback<SendMessage, SendResponse>() {
            @Override
            public void onResponse(SendMessage sendMessage, SendResponse sendResponse) {
                if (!sendResponse.isOk()) {
                    log.error("Запрос вернул ошибку {}", sendResponse);
                }
            }

            @Override
            public void onFailure(SendMessage sendMessage, IOException e) {
                log.error("Произошла ошибка при запросе", e);
            }
        });
    }

    private void setCommands() {
        SetMyCommands setMyCommands = new SetMyCommands(dispatcher.getCommands().entrySet().stream()
                .map(entry -> new BotCommand(entry.getKey(), entry.getValue().description()))
                .toArray(BotCommand[]::new));
        bot.execute(setMyCommands);
    }

    private class TgUpdatesListener implements UpdatesListener {

        @Override
        public int process(List<Update> list) {
            for (Update update : list) {
                if (update.message() == null) {
                    continue;
                }
                long chatId = update.message().chat().id();
                try {
                    dispatcher
                            .dispatchCommand(update)
                            .ifPresentOrElse(
                                    c -> c.execute(update).ifPresent(s -> sendMessage(chatId, s)),
                                    () -> sendMessage(
                                            chatId,
                                            "Команда не найдена, для просмотра доступных команд введите /help"));

                } catch (ApiCallException ex) {
                    try (var ignored1 = MDC.putCloseable("url", ex.url());
                            var ignored2 = MDC.putCloseable("code", String.valueOf(ex.code()))) {
                        log.error("Ошибка при обращении к скрапперу", ex);
                    }
                    sendMessage(chatId, ex.description());
                } catch (InvalidCommandSyntaxException ex) {
                    try (var ignored = MDC.putCloseable("command", ex.command())) {
                        log.warn("Неверный формат команды", ex);
                    }
                    sendMessage(chatId, ex.getMessage());
                } catch (RuntimeException ex) {
                    log.error("Произошла ошибка", ex);
                    sendMessage(chatId, "Произошла ошибка");
                }
            }
            return CONFIRMED_UPDATES_ALL;
        }
    }
}
