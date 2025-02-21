package backend.academy.scrapper.mapper;

import backend.academy.scrapper.entity.LinkData;
import backend.academy.shared.dto.AddLinkRequest;
import backend.academy.shared.dto.LinkResponse;
import backend.academy.shared.dto.LinkUpdate;
import java.util.List;
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

        return linkData;
    }

    public LinkUpdate createLinkUpdate(long id, String url, String description, List<Long> chatIds) {
        return new LinkUpdate(id, url, description, chatIds);
    }
}
