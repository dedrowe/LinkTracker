package backend.academy.scrapper.controller;

import backend.academy.shared.dto.AddLinkRequest;
import backend.academy.shared.dto.LinkResponse;
import backend.academy.shared.dto.ListLinkResponse;
import backend.academy.shared.dto.RemoveLinkRequest;
import backend.academy.scrapper.service.LinkDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("links")
public class LinkDataController {

    private final LinkDataService linkDataService;

    @Autowired
    public LinkDataController(LinkDataService linkDataService) {
        this.linkDataService = linkDataService;
    }

    @GetMapping
    public ListLinkResponse getByChatId(long chatId) {
        return linkDataService.getByChatId(chatId);
    }

    @PostMapping
    public LinkResponse trackLink(@RequestParam long chatId, @RequestBody AddLinkRequest request) {
        return linkDataService.trackLink(chatId, request);
    }

    @DeleteMapping
    public LinkResponse untrackLink(@RequestParam long chatId, @RequestBody RemoveLinkRequest request) {
        return linkDataService.untrackLink(chatId, request);
    }
}
