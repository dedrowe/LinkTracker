package backend.academy.bot;

import backend.academy.shared.validation.url.Url;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record BotConfig(@Valid Telegram telegram, @Valid Scrapper scrapper) {

    public record Telegram(@NotEmpty String token) {}

    public record Scrapper(@NotEmpty @Url String url) {}
}
