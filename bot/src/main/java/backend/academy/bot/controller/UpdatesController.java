package backend.academy.bot.controller;

import backend.academy.bot.TgBot;
import backend.academy.shared.dto.LinkUpdate;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/updates")
public class UpdatesController {

    private final TgBot bot;

    @PostMapping
    public void postUpdates(@RequestBody LinkUpdate update) {
        for (long chatId : update.tgChatIds()) {
            bot.sendMessage(chatId, update.description());
        }
    }
}
