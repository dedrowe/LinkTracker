package backend.academy.bot;

import backend.academy.shared.validation.url.Url;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record BotConfig(
        @Valid Telegram telegram,
        @Valid Scrapper scrapper,
        @Valid Redis redis,
        @Valid KafkaProperties kafka,
        @Valid RetryProperties retry,
        @Valid TimeoutProperties timeout,
        @NotNull MessageTransport transport) {

    public record Telegram(@NotEmpty String token) {}

    public record Scrapper(@NotEmpty @Url String url) {}

    public record Redis(@Positive int ttlMinutes) {}

    public record KafkaProperties(@NotEmpty String topic, @NotEmpty String dltTopic) {}

    public record RetryProperties(@PositiveOrZero int maxAttempts, @PositiveOrZero int backoff) {}

    public record TimeoutProperties(@Positive int connection, @Positive int read) {}

    public enum MessageTransport {
        HTTP,
        KAFKA
    }
}
