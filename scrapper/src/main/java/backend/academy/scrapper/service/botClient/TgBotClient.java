package backend.academy.scrapper.service.botClient;

import backend.academy.shared.dto.LinkUpdate;

public interface TgBotClient {

    void sendUpdates(LinkUpdate updates);
}
