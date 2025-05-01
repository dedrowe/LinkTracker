package backend.academy.scrapper.controller;

import backend.academy.scrapper.service.TgChatService;
import backend.academy.shared.dto.TgChatUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("tg-chat")
@RequiredArgsConstructor
public class TgChatController {

    private final TgChatService tgChatService;

    @PostMapping("/{id}")
    public ResponseEntity<Void> registerTgChat(@PathVariable("id") long id) {
        tgChatService.registerTgChat(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateTgChat(
            @PathVariable("id") long id, @RequestBody TgChatUpdateDto tgChatUpdateDto) {
        tgChatService.updateTgChat(id, tgChatUpdateDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTgChat(@PathVariable("id") long id) {
        tgChatService.deleteTgChat(id);
        return ResponseEntity.ok().build();
    }
}
