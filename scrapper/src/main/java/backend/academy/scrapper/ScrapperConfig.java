package backend.academy.scrapper;

import backend.academy.shared.validation.url.Url;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ScrapperConfig(
        @Valid GithubCredentials github, @Valid StackOverflowCredentials stackOverflow, @Valid Bot bot) {

    public record Bot(@NotEmpty String url) {}

    public record GithubCredentials(@NotEmpty String githubToken, @NotEmpty @Url String githubBaseUrl) {}

    public record StackOverflowCredentials(
            @NotEmpty String key, @NotEmpty String accessToken, @NotEmpty @Url String SOBaseUrl) {}
}
