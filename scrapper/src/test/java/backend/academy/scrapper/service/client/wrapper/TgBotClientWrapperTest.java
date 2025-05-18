package backend.academy.scrapper.service.client.wrapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import backend.academy.scrapper.service.ScrapperContainers;
import backend.academy.scrapper.service.botClient.HttpTgBotClient;
import backend.academy.scrapper.service.botClient.KafkaTgBotClient;
import backend.academy.scrapper.service.botClient.TgBotClientWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class TgBotClientWrapperTest extends ScrapperContainers {

    @MockitoBean
    private HttpTgBotClient httpTgBotClient;

    @MockitoBean
    private KafkaTgBotClient kafkaTgBotClient;

    @Autowired
    private TgBotClientWrapper tgBotClientWrapper;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("app.transport", () -> "http");
    }

    @Test
    public void fallbackTest() {
        doThrow(new RuntimeException()).when(httpTgBotClient).sendUpdates(any());

        tgBotClientWrapper.sendUpdates(any());

        verify(kafkaTgBotClient, times(1)).sendUpdates(any());
    }
}
