package backend.academy.bot.service;

import backend.academy.bot.TgBot;
import backend.academy.shared.dto.LinkUpdate;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UpdatesService {

    private final TgBot bot;

    public void sendUpdates(LinkUpdate linkUpdate) {
        for (long chatId : linkUpdate.tgChatIds()) {
            bot.sendMessage(chatId, linkUpdate.description());
        }
    }
}
