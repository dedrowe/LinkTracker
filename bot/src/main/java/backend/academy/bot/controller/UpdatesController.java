package backend.academy.bot.controller;

import backend.academy.bot.service.UpdatesService;
import backend.academy.shared.dto.LinkUpdate;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/updates")
@ConditionalOnProperty(havingValue = "http", prefix = "app", name = "transport")
public class UpdatesController {

    private final UpdatesService updatesService;

    @PostMapping
    public void postUpdates(@RequestBody LinkUpdate update) {
        updatesService.sendUpdates(update);
    }
}
