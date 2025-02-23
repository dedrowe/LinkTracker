package backend.academy.scrapper.service;

import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.entity.TgChat;
import backend.academy.scrapper.exceptionHandling.exceptions.NotFoundException;
import backend.academy.scrapper.repository.linkdata.LinkDataRepository;
import backend.academy.scrapper.repository.tgchat.TgChatRepository;
import backend.academy.scrapper.utils.FutureUnwrapper;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class TgChatService {

    public final TgChatRepository tgChatRepository;

    public final LinkDataRepository linkDataRepository;

    public void registerTgChat(long chatId) {
        TgChat tgChat = new TgChat(chatId);
        FutureUnwrapper.unwrap(tgChatRepository.create(tgChat));
    }

    public void deleteTgChat(long chatId) {
        TgChat tgChat = checkOptional(FutureUnwrapper.unwrap(tgChatRepository.getByChatId(chatId)), chatId);
        List<LinkData> links = FutureUnwrapper.unwrap(linkDataRepository.getByChatId(tgChat.id()));
        for (LinkData linkData : links) {
            FutureUnwrapper.unwrap(linkDataRepository.delete(linkData));
        }
        FutureUnwrapper.unwrap(tgChatRepository.delete(tgChat));
    }

    /**
     * Возвращает TgChat с переданным id, если он существует, иначе выбрасывает NotRegisteredException
     *
     * @param id id чата
     * @return TgChat с переданным id
     * @throws NotFoundException если чат не найден
     */
    public TgChat getById(long id) {
        return checkOptional(FutureUnwrapper.unwrap(tgChatRepository.getById(id)), id);
    }

    /**
     * Возвращает TgChat с переданным chatId, если он существует, иначе выбрасывает NotRegisteredException
     *
     * @param chatId id чата в telegram
     * @return TgChat с переданным chatId
     * @throws NotFoundException если чат не найден
     */
    public TgChat getByChatId(long chatId) {
        return checkOptional(FutureUnwrapper.unwrap(tgChatRepository.getByChatId(chatId)), chatId);
    }

    @SuppressWarnings("PMD.UnusedLocalVariable")
    private TgChat checkOptional(Optional<TgChat> chat, long id) {
        return chat.orElseThrow(() -> {
            String exceptionMessage = "Чат с таким id не зарегистрирован";
            NotFoundException ex = new NotFoundException(exceptionMessage);
            try (var var = MDC.putCloseable("chatId", String.valueOf(id))) {
                log.error(exceptionMessage, ex);
            }
            return ex;
        });
    }
}
