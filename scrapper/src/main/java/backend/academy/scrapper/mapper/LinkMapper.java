package backend.academy.scrapper.mapper;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.entity.TgChat;
import backend.academy.shared.dto.LinkResponse;
import backend.academy.shared.dto.LinkUpdate;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class LinkMapper {

    public LinkResponse createLinkResponse(LinkData linkData, String link, List<String> tags, List<String> filters) {
        return new LinkResponse(linkData.id(), link, tags, filters);
    }

    public LinkData createLinkData(TgChat tgChat, Link link) {
        return new LinkData(link, tgChat);
    }

    public Link createLink(String link) {
        return new Link(link);
    }

    public LinkUpdate createLinkUpdate(long id, String url, String description, List<Long> chatIds) {
        return new LinkUpdate(id, url, description, chatIds);
    }
}
