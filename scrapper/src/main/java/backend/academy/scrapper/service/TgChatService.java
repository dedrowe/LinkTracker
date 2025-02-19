package backend.academy.scrapper.service;

import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.entity.TgChat;
import backend.academy.scrapper.exceptionHandling.exceptions.NotFoundException;
import backend.academy.scrapper.repository.linkdata.LinkDataRepository;
import backend.academy.scrapper.repository.tgchat.TgChatRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TgChatService {

    public final TgChatRepository tgChatRepository;

    public final LinkDataRepository linkDataRepository;

    @Autowired
    public TgChatService(TgChatRepository tgChatRepository, LinkDataRepository linkDataRepository) {
        this.tgChatRepository = tgChatRepository;
        this.linkDataRepository = linkDataRepository;
    }

    public void registerTgChat(long chatId) {
        TgChat tgChat = new TgChat(chatId);
        tgChatRepository.create(tgChat);
    }

    public void deleteTgChat(long chatId) {
        TgChat tgChat = tgChatRepository
                .getByChatId(chatId)
                .orElseThrow(() -> new NotFoundException("Чат с таким id не зарегистрирован"));
        List<LinkData> links = linkDataRepository.getByChatId(tgChat.id());
        for (LinkData linkData : links) {
            linkDataRepository.delete(linkData);
        }
        tgChatRepository.delete(tgChat);
    }

    /**
     * Возвращает TgChat с переданным id, если он существует, иначе выбрасывает NotRegisteredException
     *
     * @param id id чата
     * @return TgChat с переданным id
     * @throws NotFoundException если чат не найден
     */
    public TgChat getById(long id) {
        return tgChatRepository
                .getById(id)
                .orElseThrow(() -> new NotFoundException("Чат с таким id не зарегистрирован"));
    }

    /**
     * Возвращает TgChat с переданным chatId, если он существует, иначе выбрасывает NotRegisteredException
     *
     * @param chatId id чата в telegram
     * @return TgChat с переданным chatId
     * @throws NotFoundException если чат не найден
     */
    public TgChat getByChatId(long chatId) {
        return tgChatRepository
                .getByChatId(chatId)
                .orElseThrow(() -> new NotFoundException("Чат с таким id не зарегистрирован"));
    }
}
