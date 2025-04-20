package backend.academy.scrapper.service.client;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.service.ScrapperContainers;
import backend.academy.scrapper.service.botClient.KafkaTgBotClient;
import backend.academy.shared.dto.LinkUpdate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class KafkaTgBotClientTest extends ScrapperContainers {

    private final String topic = "updates";

    private final KafkaTgBotClient client;

    private final ObjectMapper mapper = new ObjectMapper();

    private final KafkaTemplate<Long, String> kafkaTemplate;

    public KafkaTgBotClientTest() {
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put("bootstrap.servers", kafka.getBootstrapServers());
        producerProps.put("key.serializer", "org.apache.kafka.common.serialization.LongSerializer");
        producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put("acks", "all");

        DefaultKafkaProducerFactory<Long, String> producerFactory = new DefaultKafkaProducerFactory<>(producerProps);
        producerFactory.setTransactionIdPrefix("tx-test-");
        kafkaTemplate = new KafkaTemplate<>(producerFactory);
        this.client = new KafkaTgBotClient(kafkaTemplate, topic, new ObjectMapper());

        try (AdminClient adminClient = AdminClient.create(Map.of("bootstrap.servers", kafka.getBootstrapServers()))) {
            adminClient
                    .createTopics(List.of(new NewTopic(topic, 1, (short) 1)))
                    .all()
                    .get();
        } catch (Exception ignored) {
        }
    }

    @Test
    public void sendUpdatesSuccessTest() throws JsonProcessingException {
        try (KafkaConsumer<Long, String> consumer = createConsumer()) {
            LinkUpdate expectedResult = new LinkUpdate(1, "test", "description", List.of(1L));
            consumer.subscribe(Collections.singletonList(topic));

            kafkaTemplate.executeInTransaction(ops -> {
                client.sendUpdates(expectedResult);
                return true;
            });
            ConsumerRecord<Long, String> actualResult = KafkaTestUtils.getSingleRecord(consumer, topic);

            assertThat(actualResult.key()).isEqualTo(expectedResult.id());
            assertThat((mapper.readValue(actualResult.value(), LinkUpdate.class)))
                    .isEqualTo(expectedResult);
        }
    }

    private KafkaConsumer<Long, String> createConsumer() {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new KafkaConsumer<>(consumerProps);
    }
}
