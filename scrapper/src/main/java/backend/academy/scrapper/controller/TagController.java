package backend.academy.scrapper.controller;

import backend.academy.scrapper.service.LinkDataService;
import backend.academy.scrapper.service.TagService;
import backend.academy.shared.dto.ListLinkResponse;
import backend.academy.shared.dto.ListTagLinkCount;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("tag")
@AllArgsConstructor
public class TagController {

    private final TagService tagService;

    private final LinkDataService linkDataService;

    @GetMapping("/links/count")
    public ListTagLinkCount getTagsLinksCount(@RequestParam("Tg-Chat-Id") long chatId) {
        return tagService.getTagLinksCount(chatId);
    }

    @GetMapping("/links")
    public ListLinkResponse getLinksByTag(@RequestParam("Tg-Chat-Id") long chatId, @RequestParam("tag") String tag) {
        return linkDataService.getLinksByTagAndChatId(tag, chatId);
    }
}
