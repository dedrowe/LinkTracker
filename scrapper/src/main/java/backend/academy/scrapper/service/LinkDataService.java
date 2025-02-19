package backend.academy.scrapper.service;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.entity.TgChat;
import backend.academy.scrapper.exceptionHandling.exceptions.NotFoundException;
import backend.academy.scrapper.mapper.LinkMapper;
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.repository.linkdata.LinkDataRepository;
import backend.academy.shared.dto.AddLinkRequest;
import backend.academy.shared.dto.LinkResponse;
import backend.academy.shared.dto.ListLinkResponse;
import backend.academy.shared.dto.RemoveLinkRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LinkDataService {

    private final LinkDataRepository linkDataRepository;

    private final LinkRepository linkRepository;

    private final TgChatService tgChatService;

    private final LinkMapper linkMapper;

    private final UpdatesCheckerService updatesCheckerService;

    @Autowired
    public LinkDataService(
            LinkDataRepository linkDataRepository,
            LinkRepository linkRepository,
            TgChatService tgChatService,
            LinkMapper mapper,
            UpdatesCheckerService updatesCheckerService) {
        this.linkDataRepository = linkDataRepository;
        this.linkRepository = linkRepository;
        this.tgChatService = tgChatService;
        this.linkMapper = mapper;
        this.updatesCheckerService = updatesCheckerService;
    }

    public ListLinkResponse getByChatId(long chatId) {
        TgChat tgChat = tgChatService.getByChatId(chatId);
        List<LinkData> links = linkDataRepository.getByChatId(tgChat.id());
        List<LinkResponse> linkResponses = new ArrayList<>(links.size());
        for (LinkData linkData : links) {
            Link link = linkRepository
                    .getById(linkData.linkId())
                    .orElseThrow(
                            () -> new RuntimeException("Произошла ошибка при получении зарегистрированных ссылок"));
            linkResponses.add(linkMapper.createLinkResponse(linkData, link.link()));
        }
        return new ListLinkResponse(linkResponses, linkResponses.size());
    }

    public LinkResponse trackLink(long chatId, AddLinkRequest request) {
        TgChat tgChat = tgChatService.getByChatId(chatId);
        updatesCheckerService.checkResource(request.link());
        Optional<Link> optionalLink = linkRepository.getByLink(request.link());
        if (optionalLink.isEmpty()) {
            linkRepository.create(new Link(request.link()));
            optionalLink = linkRepository.getByLink(request.link());
        }
        Link link = optionalLink.orElseThrow();
        LinkData linkData = linkMapper.createLinkData(request, tgChat.id(), link.id());
        if (linkDataRepository.getByChatIdLinkId(tgChat.id(), link.id()).isEmpty()) {
            linkDataRepository.create(linkData);
        } else {
            linkDataRepository.update(linkData);
        }
        return linkMapper.createLinkResponse(linkData, link.link());
    }

    public LinkResponse untrackLink(long chatId, RemoveLinkRequest request) {
        TgChat tgChat = tgChatService.getByChatId(chatId);
        Link link =
                linkRepository.getByLink(request.link()).orElseThrow(() -> new NotFoundException("Ссылка не найдена"));
        LinkData linkData = linkDataRepository
                .getByChatIdLinkId(tgChat.id(), link.id())
                .orElseThrow(() -> new NotFoundException("Ссылка не найдена"));
        linkDataRepository.delete(linkData);
        return linkMapper.createLinkResponse(linkData, link.link());
    }
}
