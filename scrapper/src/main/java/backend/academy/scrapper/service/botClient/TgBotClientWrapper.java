package backend.academy.scrapper.service.botClient;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.shared.dto.LinkUpdate;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class TgBotClientWrapper {

    private final TgBotClient primaryTgBotClient;

    private final TgBotClient secondaryTgBotClient;

    public TgBotClientWrapper(
            ScrapperConfig config,
            @Qualifier("httpTgBotClient") TgBotClient httpTgBotClient,
            @Qualifier("kafkaTgBotClient") TgBotClient kafkaTgBotClient) {
        ScrapperConfig.MessageTransport transport = config.transport();
        switch (transport) {
            case KAFKA -> {
                primaryTgBotClient = kafkaTgBotClient;
                secondaryTgBotClient = httpTgBotClient;
            }
            default -> {
                primaryTgBotClient = httpTgBotClient;
                secondaryTgBotClient = kafkaTgBotClient;
            }
        }
    }

    @Retry(name = "updates-sender", fallbackMethod = "sendUpdatesFallback")
    public void sendUpdates(LinkUpdate update) {
        primaryTgBotClient.sendUpdates(update);
    }

    @Retry(name = "updates-sender")
    @SuppressWarnings({"UnusedVariable", "UnusedMethod", "PMD.UnusedPrivateMethod", "PMD.UnusedFormalParameter"})
    private void sendUpdatesFallback(LinkUpdate update, Exception e) throws Exception {
        secondaryTgBotClient.sendUpdates(update);
    }
}
