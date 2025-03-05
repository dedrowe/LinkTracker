package backend.academy.scrapper.service;

import static backend.academy.scrapper.utils.FutureUnwrapper.unwrap;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.entity.TgChat;
import backend.academy.scrapper.exceptionHandling.exceptions.LinkDataException;
import backend.academy.scrapper.exceptionHandling.exceptions.LinkException;
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
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public ListLinkResponse getByChatId(long chatId) {
        TgChat tgChat = tgChatService.getByChatId(chatId);
        List<LinkData> links = unwrap(linkDataRepository.getByChatId(tgChat.id()));
        List<LinkResponse> linkResponses = new ArrayList<>(links.size());
        CompletableFuture<Optional<Link>>[] futures = links.stream()
                .map(link -> linkRepository.getById(link.linkId()))
                .toArray(CompletableFuture[]::new);
        for (int i = 0; i < futures.length; ++i) {
            int finalI = i;
            Link link = unwrap(futures[finalI])
                    .orElseThrow(() -> new LinkDataException(
                            "Произошла ошибка при получении зарегистрированных ссылок",
                            String.valueOf(links.get(finalI).linkId()),
                            String.valueOf(chatId)));
            linkResponses.add(linkMapper.createLinkResponse(links.get(finalI), link.link()));
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

    public LinkResponse untrackLink(long chatId, RemoveLinkRequest request) {
        TgChat tgChat = tgChatService.getByChatId(chatId);
        Link link = unwrap(linkRepository.getByLink(request.link()))
                .orElseThrow(() -> new LinkException("Ссылка не найдена", request.link()));
        LinkData linkData = unwrap(linkDataRepository.getByChatIdLinkId(tgChat.id(), link.id()))
                .orElseThrow(() -> new LinkDataException(
                        "Данные о ссылке не найдены", String.valueOf(link.id()), String.valueOf(tgChat.id())));
        unwrap(linkDataRepository.delete(linkData));
        return linkMapper.createLinkResponse(linkData, link.link());
    }
}
