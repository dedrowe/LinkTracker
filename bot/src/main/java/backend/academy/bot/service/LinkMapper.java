package backend.academy.bot.service;

import backend.academy.bot.dto.LinkState;
import backend.academy.shared.dto.AddLinkRequest;
import org.springframework.stereotype.Service;

@Service
public class LinkMapper {

    public AddLinkRequest createAddLinkRequest(LinkState linkState) {
        return new AddLinkRequest(linkState.link(), linkState.tags(), linkState.filters());
    }
}
