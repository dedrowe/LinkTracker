package backend.academy.scrapper.repository.linkdata;

import backend.academy.scrapper.entity.LinkData;
import java.util.List;
import java.util.Optional;

public interface LinkDataRepository {

    <T extends LinkData> List<T> getAll();

    <T extends LinkData> Optional<T> getById(long id);

    <T extends LinkData> List<T> getByChatId(long chatId);

    <T extends LinkData> List<T> getByLinkId(long linkId);

    <T extends LinkData> List<T> getByLinkId(long linkId, long minId, long limit);

    <T extends LinkData> Optional<T> getByChatIdLinkId(long chatId, long linkId);

    <T extends LinkData> List<T> getByTagAndChatId(String tag, long chatId);

    void create(LinkData linkData);

    void deleteById(long id);

    void deleteLinkData(LinkData link);

    void deleteByChatId(long chatId);
}
