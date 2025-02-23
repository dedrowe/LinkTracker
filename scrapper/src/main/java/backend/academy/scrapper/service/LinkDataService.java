package backend.academy.scrapper.service;

import static backend.academy.scrapper.utils.FutureUnwrapper.unwrap;

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
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class LinkDataService {

    private final LinkDataRepository linkDataRepository;

    private final LinkRepository linkRepository;

    private final TgChatService tgChatService;

    private final LinkMapper linkMapper;

    private final UpdatesCheckerService updatesCheckerService;

    @SuppressWarnings("PMD.UnusedLocalVariable")
    public ListLinkResponse getByChatId(long chatId) {
        TgChat tgChat = tgChatService.getByChatId(chatId);
        List<LinkData> links = unwrap(linkDataRepository.getByChatId(tgChat.id()));
        List<LinkResponse> linkResponses = new ArrayList<>(links.size());
        for (LinkData linkData : links) {
            Link link = unwrap(linkRepository.getById(linkData.linkId())).orElseThrow(() -> {
                String exceptionMessage = "Произошла ошибка при получении зарегистрированных ссылок";
                RuntimeException ex = new RuntimeException(exceptionMessage);
                try (var var1 = MDC.putCloseable("chatId", String.valueOf(chatId));
                        var var2 = MDC.putCloseable("linkId", String.valueOf(linkData.linkId()))) {
                    log.error(exceptionMessage, ex);
                }
                return ex;
            });
            linkResponses.add(linkMapper.createLinkResponse(linkData, link.link()));
        }
        return new ListLinkResponse(linkResponses, linkResponses.size());
    }

    public LinkResponse trackLink(long chatId, AddLinkRequest request) {
        TgChat tgChat = tgChatService.getByChatId(chatId);
        updatesCheckerService.checkResource(request.link());

        Optional<Link> optionalLink = unwrap(linkRepository.getByLink(request.link()));
        if (optionalLink.isEmpty()) {
            unwrap(linkRepository.create(new Link(request.link())));
            optionalLink = unwrap(linkRepository.getByLink(request.link()));
        }
        Link link = optionalLink.orElseThrow();

        LinkData linkData = linkMapper.createLinkData(request, tgChat.id(), link.id());
        if (unwrap(linkDataRepository.getByChatIdLinkId(tgChat.id(), link.id())).isEmpty()) {
            unwrap(linkDataRepository.create(linkData));
        } else {
            unwrap(linkDataRepository.update(linkData));
        }

        return linkMapper.createLinkResponse(linkData, link.link());
    }

    @SuppressWarnings("PMD.UnusedLocalVariable")
    public LinkResponse untrackLink(long chatId, RemoveLinkRequest request) {
        TgChat tgChat = tgChatService.getByChatId(chatId);
        Link link = unwrap(linkRepository.getByLink(request.link())).orElseThrow(() -> {
            String exceptionMessage = "Ссылка не найдена";
            NotFoundException ex = new NotFoundException(exceptionMessage);
            try (var var1 = MDC.putCloseable("chatId", String.valueOf(chatId));
                    var var2 = MDC.putCloseable("link", String.valueOf(request.link()))) {
                log.error(exceptionMessage, ex);
            }
            return ex;
        });
        LinkData linkData = unwrap(linkDataRepository.getByChatIdLinkId(tgChat.id(), link.id()))
                .orElseThrow(() -> {
                    String exceptionMessage = "Данные о ссылке не найдены";
                    NotFoundException ex = new NotFoundException(exceptionMessage);
                    try (var var1 = MDC.putCloseable("chatId", String.valueOf(tgChat.id()));
                            var var2 = MDC.putCloseable("linkId", String.valueOf(link.id()))) {
                        log.error(exceptionMessage, ex);
                    }
                    return ex;
                });
        unwrap(linkDataRepository.delete(linkData));
        return linkMapper.createLinkResponse(linkData, link.link());
    }
}
