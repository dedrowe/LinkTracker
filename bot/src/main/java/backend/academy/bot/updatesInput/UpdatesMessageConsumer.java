package backend.academy.bot.updatesInput;

import backend.academy.bot.service.UpdatesService;
import backend.academy.shared.dto.LinkUpdate;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(havingValue = "kafka", prefix = "app", name = "transport")
@AllArgsConstructor
public class UpdatesMessageConsumer {

    private final UpdatesService updatesService;

    @KafkaListener(containerFactory = "defaultConsumerFactory", topics = "${app.kafka.topic}")
    @RetryableTopic(
            backoff = @Backoff(delay = 3000L),
            attempts = "2",
            autoCreateTopics = "false",
            kafkaTemplate = "defaultKafkaTemplate",
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            include = JsonProcessingException.class)
    public void consume(ConsumerRecord<Long, LinkUpdate> record, Acknowledgment ack) {
        updatesService.sendUpdates(record.value());
        ack.acknowledge();
    }
}
