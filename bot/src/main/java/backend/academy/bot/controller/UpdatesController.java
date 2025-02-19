package backend.academy.bot.controller;

import backend.academy.bot.service.TgApiService;
import backend.academy.shared.dto.LinkUpdate;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/updates")
@AllArgsConstructor
public class UpdatesController {

    private final TgApiService tgApiService;

    @PostMapping
    public void postUpdates(@RequestBody LinkUpdate update) {
        for (long chatId : update.tgChatIds()) {
            tgApiService.sendMessage(chatId, update.description());
        }
    }
}
