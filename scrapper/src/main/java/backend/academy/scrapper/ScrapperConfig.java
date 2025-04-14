package backend.academy.scrapper;

import backend.academy.shared.validation.url.Url;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ScrapperConfig(
        @Valid GithubCredentials github,
        @Valid StackOverflowCredentials stackOverflow,
        @Valid UpdatesChecker updatesChecker,
        @Valid UpdatesSender updatesSender,
        @Valid Bot bot,
        @Valid KafkaProperties kafka,
        @NotNull DbAccessType accessType,
        @NotNull MessageTransport transport) {

    public record Bot(@NotEmpty String url) {}

    public record GithubCredentials(@NotEmpty String githubToken, @NotEmpty @Url String githubBaseUrl) {}

    public record StackOverflowCredentials(
            @NotEmpty String key, @NotEmpty String accessToken, @NotEmpty @Url String SOBaseUrl) {}

    public record UpdatesChecker(
            @Positive int batchSize, @Positive int threadsCount, @PositiveOrZero int checkIntervalSeconds) {}

    public record UpdatesSender(@Positive int batchSize) {}

    public record KafkaProperties(
            @NotEmpty String topic,
            @NotEmpty String dltTopic,
            @NotEmpty String txId,
            @Positive int partitions,
            @Positive short replicas) {}

    public enum MessageTransport {
        HTTP,
        KAFKA
    }

    public enum DbAccessType {
        SQL,
        ORM
    }

    public KafkaAdmin.NewTopics createTopics() {
        return new KafkaAdmin.NewTopics(
                new NewTopic(kafka.topic, kafka.partitions, kafka.replicas),
                new NewTopic(kafka.dltTopic, kafka.partitions, kafka.replicas));
    }
}
