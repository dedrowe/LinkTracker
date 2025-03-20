package backend.academy.scrapper.mapper;

import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.entity.Tag;
import backend.academy.scrapper.entity.TgChat;
import backend.academy.shared.dto.LinkResponse;
import backend.academy.shared.dto.LinkUpdate;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class LinkMapper {

    public LinkResponse createLinkResponse(LinkData linkData, String link, List<Tag> tags, List<Filter> filters) {
        return new LinkResponse(
                linkData.id(),
                link,
                tags.stream().map(Tag::tag).toList(),
                filters.stream().map(Filter::filter).toList());
    }

    public LinkData createLinkData(long chatId, long linkId) {
        LinkData linkData = new LinkData();

        linkData.linkId(linkId);
        linkData.chatId(chatId);

        return linkData;
    }

    public LinkData createLinkData(TgChat tgChat, Link link) {
        LinkData linkData = new LinkData();

        linkData.link(link);
        linkData.linkId(link.id());
        linkData.tgChat(tgChat);
        linkData.chatId(tgChat.id());

        return linkData;
    }

    public Link createLink(String link) {
        return new Link(link);
    }

    public LinkUpdate createLinkUpdate(long id, String url, String description, List<Long> chatIds) {
        return new LinkUpdate(id, url, description, chatIds);
    }
}
