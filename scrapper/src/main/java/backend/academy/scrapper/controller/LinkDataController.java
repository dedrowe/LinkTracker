package backend.academy.scrapper.controller;

import backend.academy.scrapper.rateLimiter.RateLimit;
import backend.academy.scrapper.service.LinkDataService;
import backend.academy.scrapper.service.TagsService;
import backend.academy.shared.dto.AddLinkRequest;
import backend.academy.shared.dto.LinkResponse;
import backend.academy.shared.dto.ListLinkResponse;
import backend.academy.shared.dto.ListTagLinkCount;
import backend.academy.shared.dto.RemoveLinkRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("links")
@RequiredArgsConstructor
public class LinkDataController {

    private static final String TG_CHAT_ID_PARAM_NAME = "Tg-Chat-Id";

    private final LinkDataService linkDataService;

    private final TagsService tagsService;

    @GetMapping
    @RateLimit
    public ListLinkResponse getByChatId(
            @RequestParam(TG_CHAT_ID_PARAM_NAME) long chatId,
            @RequestParam(value = "tag", required = false) String tag) {
        if (tag != null) {
            return linkDataService.getLinksByTagAndChatId(tag, chatId);
        }
        return linkDataService.getByChatId(chatId);
    }

    @GetMapping("/tags")
    @RateLimit
    public ListTagLinkCount getTagsLinksCount(@RequestParam(TG_CHAT_ID_PARAM_NAME) long chatId) {
        return tagsService.getTagLinksCountByChatIdSync(chatId);
    }

    @PostMapping
    @RateLimit
    public LinkResponse trackLink(
            @RequestParam(TG_CHAT_ID_PARAM_NAME) long chatId, @RequestBody AddLinkRequest request) {
        return linkDataService.trackLink(chatId, request);
    }

    @DeleteMapping
    @RateLimit
    public LinkResponse untrackLink(
            @RequestParam(TG_CHAT_ID_PARAM_NAME) long chatId, @RequestBody RemoveLinkRequest request) {
        return linkDataService.untrackLink(chatId, request);
    }
}
