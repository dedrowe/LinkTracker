package backend.academy.scrapper.service.botClient;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.shared.dto.LinkUpdate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@AllArgsConstructor
public class KafkaTgBotClient implements TgBotClient {

    private final KafkaTemplate<Long, String> template;

    private final String topic;

    private final ObjectMapper mapper;

    @Autowired
    public KafkaTgBotClient(KafkaTemplate<Long, String> template, ScrapperConfig config, ObjectMapper mapper) {
        this.template = template;
        topic = config.kafka().topic();
        this.mapper = mapper;
    }

    @Override
    @Transactional
    @SuppressWarnings("FutureReturnValueIgnored")
    public void sendUpdates(LinkUpdate updates) {
        try {
            String json = mapper.writeValueAsString(updates);
            template.send(topic, updates.id(), json);
        } catch (JsonProcessingException e) {
            log.error("Ошибка при отправке обновления", e);
        }
    }
}
