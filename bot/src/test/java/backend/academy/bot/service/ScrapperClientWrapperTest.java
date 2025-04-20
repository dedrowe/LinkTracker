package backend.academy.bot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.bot.TestcontainersConfiguration;
import backend.academy.bot.service.apiClient.ScrapperClient;
import backend.academy.bot.service.apiClient.wrapper.ScrapperClientWrapper;
import backend.academy.shared.dto.AddLinkRequest;
import backend.academy.shared.dto.LinkResponse;
import backend.academy.shared.dto.ListLinkResponse;
import backend.academy.shared.dto.ListTagLinkCount;
import backend.academy.shared.dto.RemoveLinkRequest;
import backend.academy.shared.dto.TagLinkCount;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.testcontainers.containers.GenericContainer;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class ScrapperClientWrapperTest {

    private final Duration ttl = Duration.ofMinutes(60);

    private final RedisTemplate<String, ListLinkResponse> listLinkRedisTemplate;

    private final RedisTemplate<String, ListTagLinkCount> tagLinkCountRedisTemplate;

    private final ValueOperations<String, ListLinkResponse> valueOperations;

    private final ValueOperations<String, ListTagLinkCount> tagValueOperations;

    private final ScrapperClient client = mock(ScrapperClient.class);

    private final ScrapperClientWrapper wrapper;

    @Autowired
    public ScrapperClientWrapperTest(@Qualifier("redisContainer") GenericContainer<?> redisContainer) {
        redisContainer.start();

        LettuceConnectionFactory lettuceConnectionFactory =
                new LettuceConnectionFactory(redisContainer.getHost(), redisContainer.getMappedPort(6379));
        lettuceConnectionFactory.afterPropertiesSet();

        RedisTemplate<String, ListLinkResponse> listLinkTemplate = new RedisTemplate<>();
        listLinkTemplate.setConnectionFactory(lettuceConnectionFactory);
        listLinkTemplate.setKeySerializer(new StringRedisSerializer());
        listLinkTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        listLinkTemplate.afterPropertiesSet();

        RedisTemplate<String, ListTagLinkCount> tagLinkCountTemplate = new RedisTemplate<>();
        tagLinkCountTemplate.setConnectionFactory(lettuceConnectionFactory);
        tagLinkCountTemplate.setKeySerializer(new StringRedisSerializer());
        tagLinkCountTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        tagLinkCountTemplate.afterPropertiesSet();

        this.listLinkRedisTemplate = spy(listLinkTemplate);
        this.tagLinkCountRedisTemplate = spy(tagLinkCountTemplate);
        this.valueOperations = spy(listLinkTemplate.opsForValue());
        this.tagValueOperations = spy(tagLinkCountTemplate.opsForValue());

        wrapper = new ScrapperClientWrapper(client, listLinkRedisTemplate, tagLinkCountRedisTemplate, ttl);
    }

    @BeforeEach
    void setUp() {
        listLinkRedisTemplate.getConnectionFactory().getConnection().flushDb();
    }

    @Test
    public void getLinksFromCacheTest() {
        ListLinkResponse expectedResult =
                new ListLinkResponse(List.of(new LinkResponse(1L, "link", List.of(), List.of())), 1);
        listLinkRedisTemplate.opsForValue().set("links:1", expectedResult);
        when(listLinkRedisTemplate.opsForValue()).thenReturn(valueOperations);

        ListLinkResponse actualResult = wrapper.getLinks(1L);

        assertThat(actualResult).isEqualTo(expectedResult);
        verify(listLinkRedisTemplate.opsForValue(), times(1)).get("links:1");
        verify(listLinkRedisTemplate.opsForValue(), times(0)).set(anyString(), any(), any(Duration.class));
        verify(client, times(0)).getLinks(anyLong());
    }

    @Test
    public void setLinksToCacheTest() {
        ListLinkResponse expectedResult = new ListLinkResponse(List.of(), 0);
        when(client.getLinks(anyLong())).thenReturn(expectedResult);
        when(listLinkRedisTemplate.opsForValue()).thenReturn(valueOperations);

        ListLinkResponse actualResult = wrapper.getLinks(1L);

        assertThat(actualResult).isEqualTo(expectedResult);
        verify(listLinkRedisTemplate.opsForValue(), times(1))
                .set(eq("links:1"), eq(expectedResult), any(Duration.class));
    }

    @Test
    public void trackLinkInvalidateTest() {
        LinkResponse linkResponse = new LinkResponse(1L, "link", List.of("1"), List.of());
        ListLinkResponse expectedResult = new ListLinkResponse(List.of(linkResponse), 1);
        ListLinkResponse response =
                new ListLinkResponse(List.of(new LinkResponse(1L, "link2", List.of("2"), List.of())), 1);
        listLinkRedisTemplate.opsForValue().set("links:1", expectedResult);
        listLinkRedisTemplate.opsForValue().set("links-tags:1/1", expectedResult);
        listLinkRedisTemplate.opsForValue().set("links-tags:1/2", response);
        when(client.trackLink(anyLong(), any())).thenReturn(linkResponse);
        when(listLinkRedisTemplate.opsForValue()).thenReturn(valueOperations);

        wrapper.trackLink(1, new AddLinkRequest("link", List.of("1"), List.of()));

        verify(listLinkRedisTemplate, times(1)).delete("links:1");
        verify(tagLinkCountRedisTemplate, times(1)).delete("tags-count:1");
        verify(listLinkRedisTemplate, times(1)).delete("links-tags:1/1");
        assertThat(listLinkRedisTemplate.opsForValue().get("links:1")).isNull();
        assertThat(listLinkRedisTemplate.opsForValue().get("links-tags:1/1")).isNull();
        assertThat(listLinkRedisTemplate.opsForValue().get("links-tags:1/2")).isEqualTo(response);
    }

    @Test
    public void untrackLinkInvalidateTest() {
        LinkResponse linkResponse = new LinkResponse(1L, "link", List.of("1"), List.of());
        ListLinkResponse expectedResult = new ListLinkResponse(List.of(linkResponse), 1);
        ListLinkResponse response =
                new ListLinkResponse(List.of(new LinkResponse(1L, "link2", List.of("2"), List.of())), 1);
        listLinkRedisTemplate.opsForValue().set("links:1", expectedResult);
        listLinkRedisTemplate.opsForValue().set("links-tags:1/1", expectedResult);
        listLinkRedisTemplate.opsForValue().set("links-tags:1/2", response);
        when(client.untrackLink(anyLong(), any())).thenReturn(linkResponse);
        when(listLinkRedisTemplate.opsForValue()).thenReturn(valueOperations);

        wrapper.untrackLink(1, new RemoveLinkRequest("link"));

        verify(listLinkRedisTemplate, times(1)).delete("links:1");
        verify(tagLinkCountRedisTemplate, times(1)).delete("tags-count:1");
        verify(listLinkRedisTemplate, times(1)).delete("links-tags:1/1");
        assertThat(listLinkRedisTemplate.opsForValue().get("links:1")).isNull();
        assertThat(listLinkRedisTemplate.opsForValue().get("links-tags:1/1")).isNull();
        assertThat(listLinkRedisTemplate.opsForValue().get("links-tags:1/2")).isEqualTo(response);
    }

    @Test
    public void getTagLinkCountFromCacheTest() {
        ListTagLinkCount expectedResult = new ListTagLinkCount(List.of(new TagLinkCount("link", 1)));
        tagLinkCountRedisTemplate.opsForValue().set("tags-count:1", expectedResult);
        when(tagLinkCountRedisTemplate.opsForValue()).thenReturn(tagValueOperations);

        ListTagLinkCount actualResult = wrapper.getTagLinksCount(1);

        assertThat(actualResult).isEqualTo(expectedResult);
        verify(tagLinkCountRedisTemplate.opsForValue(), times(1)).get("tags-count:1");
        verify(tagLinkCountRedisTemplate.opsForValue(), times(0)).set(anyString(), any(), any(Duration.class));
        verify(client, times(0)).getTagLinksCount(anyLong());
    }

    @Test
    public void setTagLinkCountToCacheTest() {
        ListTagLinkCount expectedResult = new ListTagLinkCount(List.of());
        when(client.getTagLinksCount(anyLong())).thenReturn(expectedResult);
        when(tagLinkCountRedisTemplate.opsForValue()).thenReturn(tagValueOperations);

        ListTagLinkCount actualResult = wrapper.getTagLinksCount(1);

        assertThat(actualResult).isEqualTo(expectedResult);
        verify(tagLinkCountRedisTemplate.opsForValue(), times(1))
                .set(eq("tags-count:1"), eq(expectedResult), any(Duration.class));
    }

    @Test
    public void getLinksByTagFromCacheTest() {
        ListLinkResponse expectedResult =
                new ListLinkResponse(List.of(new LinkResponse(1L, "link", List.of(), List.of())), 1);
        listLinkRedisTemplate.opsForValue().set("links-tags:1/1", expectedResult);
        when(listLinkRedisTemplate.opsForValue()).thenReturn(valueOperations);

        ListLinkResponse actualResult = wrapper.getLinksByTag(1L, "1");

        assertThat(actualResult).isEqualTo(expectedResult);
        verify(listLinkRedisTemplate.opsForValue(), times(1)).get("links-tags:1/1");
        verify(listLinkRedisTemplate.opsForValue(), times(0)).set(anyString(), any(), any(Duration.class));
        verify(client, times(0)).getLinksByTag(anyLong(), anyString());
    }

    @Test
    public void setLinksByTagToCacheTest() {
        ListLinkResponse expectedResult = new ListLinkResponse(List.of(), 0);
        when(client.getLinksByTag(anyLong(), anyString())).thenReturn(expectedResult);
        when(listLinkRedisTemplate.opsForValue()).thenReturn(valueOperations);

        ListLinkResponse actualResult = wrapper.getLinksByTag(1L, "1");

        assertThat(actualResult).isEqualTo(expectedResult);
        verify(listLinkRedisTemplate.opsForValue(), times(1))
                .set(eq("links-tags:1/1"), eq(expectedResult), any(Duration.class));
    }
}
