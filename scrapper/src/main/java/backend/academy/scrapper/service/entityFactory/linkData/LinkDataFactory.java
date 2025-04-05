package backend.academy.scrapper.service.entityFactory.linkData;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.entity.TgChat;

public interface LinkDataFactory {

    LinkData getLinkData(Link link, TgChat tgChat);

    LinkData getLinkData(Long id, Link link, TgChat tgChat);

    LinkData getLinkData(Link link, TgChat tgChat, boolean deleted);
}
