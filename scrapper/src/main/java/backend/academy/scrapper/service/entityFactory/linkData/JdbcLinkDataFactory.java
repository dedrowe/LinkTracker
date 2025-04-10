package backend.academy.scrapper.service.entityFactory.linkData;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.entity.TgChat;
import backend.academy.scrapper.entity.jdbc.JdbcLinkData;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(havingValue = "SQL", prefix = "app", name = "access-type")
public class JdbcLinkDataFactory implements LinkDataFactory {
    @Override
    public LinkData getLinkData(Link link, TgChat tgChat) {
        return new JdbcLinkData(link.id(), tgChat.id());
    }

    @Override
    public LinkData getLinkData(Long id, Link link, TgChat tgChat) {
        return new JdbcLinkData(id, link.id(), tgChat.id());
    }

    @Override
    public LinkData getLinkData(Link link, TgChat tgChat, boolean deleted) {
        return new JdbcLinkData(link.id(), tgChat.id(), deleted);
    }
}
