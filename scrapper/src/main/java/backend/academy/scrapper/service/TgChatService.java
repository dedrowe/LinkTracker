package backend.academy.scrapper.service;

import backend.academy.scrapper.entity.TgChat;
import backend.academy.scrapper.exceptionHandling.exceptions.TgChatException;
import backend.academy.scrapper.repository.linkdata.LinkDataRepository;
import backend.academy.scrapper.repository.tgchat.TgChatRepository;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class TgChatService {

    private final TgChatRepository tgChatRepository;

    private final LinkDataRepository linkDataRepository;

    public void registerTgChat(long chatId) {
        TgChat tgChat = new TgChat(chatId);
        tgChatRepository.create(tgChat);
    }

    public void deleteTgChat(long chatId) {
        TgChat tgChat = checkOptional(tgChatRepository.getByChatId(chatId), chatId);
        linkDataRepository.deleteByChatId(tgChat.id());
        tgChatRepository.delete(tgChat);
    }

    /**
     * Возвращает TgChat с переданным id, если он существует, иначе выбрасывает NotRegisteredException
     *
     * @param id id чата
     * @return TgChat с переданным id
     * @throws TgChatException если чат не найден
     */
    public TgChat getById(long id) {
        return checkOptional(tgChatRepository.getById(id), id);
    }

    /**
     * Возвращает TgChat с переданным chatId, если он существует, иначе выбрасывает NotRegisteredException
     *
     * @param chatId id чата в telegram
     * @return TgChat с переданным chatId
     * @throws TgChatException если чат не найден
     */
    public TgChat getByChatId(long chatId) {
        return checkOptional(tgChatRepository.getByChatId(chatId), chatId);
    }

    private TgChat checkOptional(Optional<TgChat> chat, long id) {
        return chat.orElseThrow(() -> new TgChatException("Чат с таким id не зарегистрирован", String.valueOf(id)));
    }
}
