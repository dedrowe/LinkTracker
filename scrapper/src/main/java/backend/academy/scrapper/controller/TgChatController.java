package backend.academy.scrapper.controller;

import backend.academy.scrapper.service.TgChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("tg-chat")
public class TgChatController {

    private final TgChatService tgChatService;

    @Autowired
    public TgChatController(TgChatService tgChatService) {
        this.tgChatService = tgChatService;
    }

    @PostMapping("/{id}")
    public ResponseEntity<Void> registerTgChat(@PathVariable("id") long id) {
        tgChatService.registerTgChat(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTgChat(@PathVariable("id") long id) {
        tgChatService.deleteTgChat(id);
        return ResponseEntity.ok().build();
    }
}
