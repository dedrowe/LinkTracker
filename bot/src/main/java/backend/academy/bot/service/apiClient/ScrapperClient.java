package backend.academy.bot.service.apiClient;

import backend.academy.bot.BotConfig;
import backend.academy.shared.dto.AddLinkRequest;
import backend.academy.shared.dto.ApiErrorResponse;
import backend.academy.shared.dto.LinkResponse;
import backend.academy.shared.dto.ListLinkResponse;
import backend.academy.shared.dto.ListTagLinkCount;
import backend.academy.shared.dto.RemoveLinkRequest;
import backend.academy.shared.dto.TgChatUpdateDto;
import backend.academy.shared.exceptions.ApiCallException;
import backend.academy.shared.utils.client.RequestFactoryBuilder;
import backend.academy.shared.utils.client.RetryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
@AllArgsConstructor
public class ScrapperClient {

    private final RestClient client;

    private final ObjectMapper mapper;

    private final RetryWrapper retryWrapper;

    @Autowired
    public ScrapperClient(
            BotConfig config, RestClient.Builder clientBuilder, ObjectMapper mapper, RetryWrapper retryWrapper) {
        client = clientBuilder
                .requestFactory(new RequestFactoryBuilder()
                        .setConnectionTimeout(config.timeout().connection())
                        .setReadTimeout(config.timeout().read())
                        .build())
                .baseUrl(config.scrapper().url())
                .build();
        this.mapper = mapper;
        this.retryWrapper = retryWrapper;
    }

    private RestClient.ResponseSpec setStatusHandler(RestClient.ResponseSpec responseSpec) {
        return responseSpec.onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
            ApiErrorResponse error = mapper.readValue(response.getBody(), ApiErrorResponse.class);
            String exceptionDescription = error.description();
            if (error.code().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
                exceptionDescription = "Произошла ошибка";
            }
            throw new ApiCallException(
                    exceptionDescription,
                    error.exceptionMessage(),
                    error.code().value(),
                    request.getURI().toString());
        });
    }

    public void registerChat(long chatId) {
        retryWrapper.retry(() -> setStatusHandler(
                        client.post().uri("/tg-chat/{id}", chatId).retrieve())
                .toBodilessEntity());
    }

    public void deleteChat(long chatId) {
        retryWrapper.retry(() -> setStatusHandler(
                        client.delete().uri("/tg-chat/{id}", chatId).retrieve())
                .toBodilessEntity());
    }

    public void updateChat(long chatId, TgChatUpdateDto dto) {
        retryWrapper
                .retry(() -> setStatusHandler(
                        client.put().uri("/tg-chat/{id}", chatId).body(dto).retrieve()))
                .toBodilessEntity();
    }

    public ListLinkResponse getLinks(long chatId) {
        return retryWrapper.retry(() -> setStatusHandler(client.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/links")
                                .queryParam("Tg-Chat-Id", chatId)
                                .build())
                        .retrieve())
                .toEntity(ListLinkResponse.class)
                .getBody());
    }

    public LinkResponse trackLink(long chatId, AddLinkRequest request) {
        return retryWrapper.retry(() -> setStatusHandler(client.post()
                        .uri(uriBuilder -> uriBuilder
                                .path("/links")
                                .queryParam("Tg-Chat-Id", chatId)
                                .build())
                        .body(request)
                        .retrieve())
                .toEntity(LinkResponse.class)
                .getBody());
    }

    public LinkResponse untrackLink(long chatId, RemoveLinkRequest request) {
        return retryWrapper.retry(() -> setStatusHandler(client.method(HttpMethod.DELETE)
                        .uri(uriBuilder -> uriBuilder
                                .path("/links")
                                .queryParam("Tg-Chat-Id", chatId)
                                .build())
                        .body(request)
                        .retrieve())
                .toEntity(LinkResponse.class)
                .getBody());
    }

    public ListTagLinkCount getTagLinksCount(long chatId) {
        return retryWrapper.retry(() -> setStatusHandler(client.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/links/tags")
                                .queryParam("Tg-Chat-Id", chatId)
                                .build())
                        .retrieve())
                .toEntity(ListTagLinkCount.class)
                .getBody());
    }

    public ListLinkResponse getLinksByTag(long chatId, String tag) {
        return retryWrapper.retry(() -> setStatusHandler(client.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/links")
                                .queryParam("Tg-Chat-Id", chatId)
                                .queryParam("tag", tag)
                                .build())
                        .retrieve())
                .toEntity(ListLinkResponse.class)
                .getBody());
    }
}
