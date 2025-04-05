package backend.academy.scrapper.service.entityFactory.linkData;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.entity.TgChat;
import backend.academy.scrapper.entity.jpa.JpaLinkData;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(havingValue = "ORM", prefix = "app", name = "access-type")
public class JpaLinkDataFactory implements LinkDataFactory {
    @Override
    public LinkData getLinkData(Link link, TgChat tgChat) {
        return new JpaLinkData(link, tgChat);
    }

    @Override
    public LinkData getLinkData(Long id, Link link, TgChat tgChat) {
        return new JpaLinkData(id, link, tgChat);
    }

    @Override
    public LinkData getLinkData(Link link, TgChat tgChat, boolean deleted) {
        return new JpaLinkData(link, tgChat, deleted);
    }
}
