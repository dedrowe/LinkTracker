package backend.academy.scrapper.mapper;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.dto.LinkResponse;
import backend.academy.scrapper.entity.LinkData;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.stereotype.Component;

@Component
public class LinkMapper {

    public LinkResponse createLinkResponse(LinkData linkData, String link) {
        return new LinkResponse(linkData.id(), link, linkData.tags(), linkData.filters());
    }

    public LinkData createLinkData(AddLinkRequest addLinkRequest, long chatId, long linkId) {
        LinkData linkData = new LinkData();

        linkData.linkId(linkId);
        linkData.chatId(chatId);
        linkData.tags(addLinkRequest.tags());
        linkData.filters(addLinkRequest.filters());
        linkData.lastCheck(LocalDateTime.now(ZoneOffset.UTC));

        return linkData;
    }
}
