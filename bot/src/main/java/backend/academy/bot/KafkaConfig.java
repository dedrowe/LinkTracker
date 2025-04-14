package backend.academy.bot;

import backend.academy.shared.dto.LinkUpdate;
import java.util.Map;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.RoundRobinAssignor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonLoggingErrorHandler;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
@AllArgsConstructor
public class KafkaConfig {

    private final KafkaProperties properties;

    @Bean
    Admin localKafkaClusterAdminClient() {
        return AdminClient.create(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrapServers()));
    }

    @Bean(name = "defaultConsumerFactory")
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<Long, LinkUpdate>>
            defaultConsumerFactory() {
        ConcurrentKafkaListenerContainerFactory<Long, LinkUpdate> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(
                LinkUpdatesDeserializer.class, props -> props.put(ConsumerConfig.GROUP_ID_CONFIG, "bot-group")));
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setCommonErrorHandler(new CommonLoggingErrorHandler());
        factory.setAutoStartup(true);
        factory.setConcurrency(1);
        return factory;
    }

    private <M> ConsumerFactory<Long, M> consumerFactory(
            Class<? extends Deserializer<M>> valueDeserializerClass, Consumer<Map<String, Object>> propsModifier) {
        var props = properties.buildConsumerProperties(null);

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, LongDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, valueDeserializerClass);

        props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, RoundRobinAssignor.class.getName());

        propsModifier.accept(props);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean(name = "defaultKafkaTemplate")
    public KafkaTemplate<Long, LinkUpdate> kafkaTemplate() {
        Map<String, Object> props = properties.buildProducerProperties(null);

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, LinkUpdatesSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "0");
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
    }

    @Bean(name = "dltKafkaTemplate")
    public KafkaTemplate<Object, Object> dltKafkaTemplate() {
        Map<String, Object> props = properties.buildProducerProperties(null);

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, LinkUpdatesSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "0");
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
    }

    public static class LinkUpdatesDeserializer extends JsonDeserializer<LinkUpdate> {}

    public static class LinkUpdatesSerializer extends JsonSerializer<LinkUpdate> {}
}
