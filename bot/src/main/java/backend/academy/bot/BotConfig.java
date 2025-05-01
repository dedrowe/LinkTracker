package backend.academy.bot;

import backend.academy.shared.validation.url.Url;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record BotConfig(
        @Valid Telegram telegram,
        @Valid Scrapper scrapper,
        @Valid Redis redis,
        @Valid KafkaProperties kafka,
        @NotNull MessageTransport transport) {

    public record Telegram(@NotEmpty String token) {}

    public record Scrapper(@NotEmpty @Url String url) {}

    public record Redis(@Positive int ttlMinutes) {}

    public record KafkaProperties(@NotEmpty String topic, @NotEmpty String dltTopic) {}

    public enum MessageTransport {
        HTTP,
        KAFKA
    }
}
