package backend.academy.bot.service;

import static backend.academy.shared.utils.client.RetryWrapper.retry;

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
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
@AllArgsConstructor
public class ScrapperClient {

    private static final String LINKS_CACHE_PREFIX = "links:";

    private static final String LINKS_TAGS_CACHE_PREFIX = "links-tags:";

    private static final String LINKS_TAGS_COUNT_PREFIX = "tags-count:";

    private final RestClient client;

    private final ObjectMapper mapper;

    private final RedisTemplate<String, ListLinkResponse> redisListLinkResponse;

    private final RedisTemplate<String, ListTagLinkCount> redisTagLinkCount;

    private final Duration ttlMinutes;

    @Autowired
    public ScrapperClient(
            BotConfig config, RestClient.Builder clientBuilder, RedisTemplate<String, ListLinkResponse> redisTemplate,
            RedisTemplate<String, ListTagLinkCount> redisTagLinkCount,
            ObjectMapper mapper) {
        client = clientBuilder
                .requestFactory(new RequestFactoryBuilder().build())
                .baseUrl(config.scrapper().url())
                .build();
        ttlMinutes = Duration.ofMinutes(config.redis().ttlMinutes());
        this.mapper = mapper;
        redisListLinkResponse = redisTemplate;
        this.redisTagLinkCount = redisTagLinkCount;
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
        retry(() -> setStatusHandler(client.post().uri("/tg-chat/{id}", chatId).retrieve())
                .toBodilessEntity());
    }

    public void deleteChat(long chatId) {
        retry(() -> setStatusHandler(
                        client.delete().uri("/tg-chat/{id}", chatId).retrieve())
                .toBodilessEntity());
    }

    public void updateChat(long chatId, TgChatUpdateDto dto) {
        retry(() -> setStatusHandler(
                        client.put().uri("/tg-chat/{id}", chatId).body(dto).retrieve()))
                .toBodilessEntity();
    }

    public ListLinkResponse getLinks(long chatId) {
        ListLinkResponse response = redisListLinkResponse.opsForValue().get(LINKS_CACHE_PREFIX + chatId);
        if (response != null) {
            return response;
        }
        response = retry(() -> setStatusHandler(client.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/links")
                                .queryParam("Tg-Chat-Id", chatId)
                                .build())
                        .retrieve())
                .toEntity(ListLinkResponse.class)
                .getBody());
        redisListLinkResponse.opsForValue().set(LINKS_CACHE_PREFIX + chatId, response, ttlMinutes);
        return response;
    }

    public void trackLink(long chatId, AddLinkRequest request) {
        LinkResponse response = retry(() -> setStatusHandler(client.post()
                        .uri(uriBuilder -> uriBuilder
                                .path("/links")
                                .queryParam("Tg-Chat-Id", chatId)
                                .build())
                        .body(request)
                        .retrieve())
                .toEntity(LinkResponse.class)
                .getBody());
        redisListLinkResponse.delete(LINKS_CACHE_PREFIX + chatId);
        redisTagLinkCount.delete(LINKS_TAGS_COUNT_PREFIX + chatId);
        for (String tag : response.tags()) {
            redisListLinkResponse.delete(LINKS_TAGS_CACHE_PREFIX + chatId + "/" + tag);
        }
    }

    public void untrackLink(long chatId, RemoveLinkRequest request) {
        LinkResponse response = retry(() -> setStatusHandler(client.method(HttpMethod.DELETE)
                        .uri(uriBuilder -> uriBuilder
                                .path("/links")
                                .queryParam("Tg-Chat-Id", chatId)
                                .build())
                        .body(request)
                        .retrieve())
                .toEntity(LinkResponse.class)
                .getBody());
        redisListLinkResponse.delete(LINKS_CACHE_PREFIX + chatId);
        redisTagLinkCount.delete(LINKS_TAGS_COUNT_PREFIX + chatId);
        for (String tag : response.tags()) {
            redisListLinkResponse.delete(LINKS_TAGS_CACHE_PREFIX + chatId + "/" + tag);
        }
    }

    public ListTagLinkCount getTagLinksCount(long chatId) {
        ListTagLinkCount response = redisTagLinkCount.opsForValue().get(LINKS_TAGS_COUNT_PREFIX + chatId);
        if (response != null) {
            return response;
        }
        response = retry(() -> setStatusHandler(client.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/links/tags")
                                .queryParam("Tg-Chat-Id", chatId)
                                .build())
                        .retrieve())
                .toEntity(ListTagLinkCount.class)
                .getBody());
        redisTagLinkCount.opsForValue().set(LINKS_TAGS_COUNT_PREFIX + chatId, response, ttlMinutes);
        return response;
    }

    public ListLinkResponse getLinksByTag(long chatId, String tag) {
        ListLinkResponse response = redisListLinkResponse.opsForValue().get(LINKS_TAGS_CACHE_PREFIX + chatId + "/" + tag);
        if (response != null) {
            return response;
        }
        response = retry(() -> setStatusHandler(client.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/links")
                                .queryParam("Tg-Chat-Id", chatId)
                                .queryParam("tag", tag)
                                .build())
                        .retrieve())
                .toEntity(ListLinkResponse.class)
                .getBody());
        redisListLinkResponse.opsForValue().set(LINKS_TAGS_CACHE_PREFIX + chatId + "/" + tag, response, ttlMinutes);
        return response;
    }
}
