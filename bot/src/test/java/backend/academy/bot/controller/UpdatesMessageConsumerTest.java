package backend.academy.bot.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import backend.academy.bot.service.UpdatesService;
import backend.academy.shared.dto.LinkUpdate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

@SpringBootTest
@Testcontainers
public class UpdatesMessageConsumerTest {

    @Container
    @ServiceConnection
    private static final KafkaContainer kafka = new KafkaContainer("apache/kafka-native:4.0.0");

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("app.kafka.topic", () -> UPDATES);
        registry.add("app.kafka.dltTopic", () -> UPDATES_DLT);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    private static final String UPDATES = "updates";

    private static final String UPDATES_DLT = "updates-dlt";

    @MockitoBean
    private UpdatesService updatesService;

    @Autowired
    private KafkaTemplate<Long, LinkUpdate> template;

    private final KafkaTemplate<Long, String> stringTemplate = stringKafkaTemplate();

    @BeforeAll
    static void beforeAll() {
        try (AdminClient adminClient = AdminClient.create(Map.of("bootstrap.servers", kafka.getBootstrapServers()))) {
            adminClient
                    .createTopics(List.of(new NewTopic(UPDATES, 1, (short) 1), new NewTopic(UPDATES_DLT, 1, (short) 1)))
                    .all()
                    .get();
        } catch (Exception ignored) {
        }
    }

    @Test
    public void getUpdateSuccessTest() throws InterruptedException {
        LinkUpdate expectedResult = new LinkUpdate(1, "test", "description", List.of(1L));
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(inv -> {
                    latch.countDown();
                    return null;
                })
                .when(updatesService)
                .sendUpdates(expectedResult);

        template.send(UPDATES, expectedResult).toCompletableFuture().join();
        template.flush();

        latch.await();
        verify(updatesService, times(1)).sendUpdates(expectedResult);
    }

    @Test
    public void getUpdateToDltTest() {
        String expectedResult = "test";

        stringTemplate.send(UPDATES, expectedResult);

        try (KafkaConsumer<Long, String> consumer = createConsumer()) {
            consumer.subscribe(List.of(UPDATES_DLT));
            assertThat(KafkaTestUtils.getSingleRecord(consumer, UPDATES_DLT).value())
                    .isEqualTo("\"test\"");
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

    public KafkaTemplate<Long, String> stringKafkaTemplate() {
        Map<String, Object> props = new HashMap<>();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "0");
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
    }
}
