package backend.academy.bot.service.apiClient.wrapper;

import backend.academy.bot.BotConfig;
import backend.academy.bot.service.apiClient.ScrapperClient;
import backend.academy.shared.dto.AddLinkRequest;
import backend.academy.shared.dto.LinkResponse;
import backend.academy.shared.dto.ListLinkResponse;
import backend.academy.shared.dto.ListTagLinkCount;
import backend.academy.shared.dto.RemoveLinkRequest;
import backend.academy.shared.dto.TgChatUpdateDto;
import java.time.Duration;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ScrapperClientWrapper {

    private static final String LINKS_CACHE_PREFIX = "links:";

    private static final String LINKS_TAGS_CACHE_PREFIX = "links-tags:";

    private static final String LINKS_TAGS_COUNT_PREFIX = "tags-count:";

    private final ScrapperClient client;

    private final RedisTemplate<String, ListLinkResponse> redisListLinkResponse;

    private final RedisTemplate<String, ListTagLinkCount> redisTagLinkCount;

    private final Duration ttlMinutes;

    @Autowired
    public ScrapperClientWrapper(
            ScrapperClient client,
            BotConfig config,
            RedisTemplate<String, ListLinkResponse> redisTemplate,
            RedisTemplate<String, ListTagLinkCount> redisTagLinkCount) {
        this.client = client;
        ttlMinutes = Duration.ofMinutes(config.redis().ttlMinutes());
        redisListLinkResponse = redisTemplate;
        this.redisTagLinkCount = redisTagLinkCount;
    }

    public void registerChat(long chatId) {
        client.registerChat(chatId);
    }

    public void updateChat(long chatId, TgChatUpdateDto dto) {
        client.updateChat(chatId, dto);
    }

    public ListLinkResponse getLinks(long chatId) {
        ListLinkResponse response = redisListLinkResponse.opsForValue().get(LINKS_CACHE_PREFIX + chatId);
        if (response != null) {
            return response;
        }

        response = client.getLinks(chatId);

        redisListLinkResponse.opsForValue().set(LINKS_CACHE_PREFIX + chatId, response, ttlMinutes);
        return response;
    }

    public void trackLink(long chatId, AddLinkRequest request) {
        LinkResponse response = client.trackLink(chatId, request);

        redisListLinkResponse.delete(LINKS_CACHE_PREFIX + chatId);
        redisTagLinkCount.delete(LINKS_TAGS_COUNT_PREFIX + chatId);
        for (String tag : response.tags()) {
            redisListLinkResponse.delete(LINKS_TAGS_CACHE_PREFIX + chatId + "/" + tag);
        }
    }

    public void untrackLink(long chatId, RemoveLinkRequest request) {
        LinkResponse response = client.untrackLink(chatId, request);

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
        response = client.getTagLinksCount(chatId);

        redisTagLinkCount.opsForValue().set(LINKS_TAGS_COUNT_PREFIX + chatId, response, ttlMinutes);
        return response;
    }

    public ListLinkResponse getLinksByTag(long chatId, String tag) {
        ListLinkResponse response =
                redisListLinkResponse.opsForValue().get(LINKS_TAGS_CACHE_PREFIX + chatId + "/" + tag);
        if (response != null) {
            return response;
        }

        response = client.getLinksByTag(chatId, tag);

        redisListLinkResponse.opsForValue().set(LINKS_TAGS_CACHE_PREFIX + chatId + "/" + tag, response, ttlMinutes);
        return response;
    }
}
