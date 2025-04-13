package backend.academy.bot.service;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.bot.TestcontainersConfiguration;
import backend.academy.shared.dto.AddLinkRequest;
import backend.academy.shared.dto.LinkResponse;
import backend.academy.shared.dto.ListLinkResponse;
import backend.academy.shared.dto.RemoveLinkRequest;
import backend.academy.shared.exceptions.ApiCallException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.GenericContainer;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class ScrapperClientTest {

    private final String mockServerAddress = "localhost";

    private final int mockServerPort = 8080;

    private final WireMockServer wireMockServer = new WireMockServer(
            WireMockConfiguration.options().bindAddress(mockServerAddress).port(mockServerPort));

    private final Duration ttl = Duration.ofMinutes(60);

    private final RedisTemplate<String, ListLinkResponse> redisTemplate;

    private final ValueOperations<String, ListLinkResponse> valueOperations;

    private final ScrapperClient scrapperClient;

    @Autowired
    public ScrapperClientTest(@Qualifier("redisContainer") GenericContainer<?> redisContainer) {
        redisContainer.start();

        LettuceConnectionFactory lettuceConnectionFactory =
                new LettuceConnectionFactory(redisContainer.getHost(), redisContainer.getMappedPort(6379));
        lettuceConnectionFactory.afterPropertiesSet();

        RedisTemplate<String, ListLinkResponse> template = new RedisTemplate<>();
        template.setConnectionFactory(lettuceConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();

        this.redisTemplate = spy(template);
        this.valueOperations = spy(template.opsForValue());
        scrapperClient = new ScrapperClient(
                RestClient.builder()
                        .baseUrl("http://" + mockServerAddress + ":" + mockServerPort)
                        .build(),
                new ObjectMapper(),
                redisTemplate,
                ttl);
    }

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().flushDb();
        wireMockServer.start();
        configureFor(mockServerAddress, wireMockServer.port());
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    public void getLinksFromCacheTest() {
        ListLinkResponse expectedResult =
                new ListLinkResponse(List.of(new LinkResponse(1L, "link", List.of(), List.of())), 1);
        redisTemplate.opsForValue().set("links:1", expectedResult);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        ListLinkResponse actualResult = scrapperClient.getLinks(1L);

        assertThat(actualResult).isEqualTo(expectedResult);
        verify(redisTemplate.opsForValue(), times(1)).get("links:1");
        verify(redisTemplate.opsForValue(), times(0)).set(anyString(), any());
    }

    @Test
    public void setLinksToCacheTest() {
        ListLinkResponse expectedResult = new ListLinkResponse(List.of(), 0);
        stubFor(get(urlPathMatching("/links*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("{\"links\":[], \"size\": 0}")));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        ListLinkResponse actualResult = scrapperClient.getLinks(1L);

        assertThat(actualResult).isEqualTo(expectedResult);
        verify(redisTemplate.opsForValue(), times(1)).set(eq("links:1"), eq(expectedResult), any());
    }

    @Test
    public void trackLinkInvalidateTest() {
        ListLinkResponse expectedResult =
                new ListLinkResponse(List.of(new LinkResponse(1L, "link", List.of("1"), List.of())), 1);
        ListLinkResponse response =
                new ListLinkResponse(List.of(new LinkResponse(1L, "link2", List.of("2"), List.of())), 1);
        redisTemplate.opsForValue().set("links:1", expectedResult);
        redisTemplate.opsForValue().set("links-tags:1/1", expectedResult);
        redisTemplate.opsForValue().set("links-tags:1/2", response);
        stubFor(post(urlPathMatching("/links*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("{\"id\": 1, \"url\": \"link\", \"tags\": [\"1\"], \"filters\": []}")));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        scrapperClient.trackLink(1, new AddLinkRequest("link", List.of("1"), List.of()));

        verify(redisTemplate, times(1)).delete("links:1");
        verify(redisTemplate, times(1)).delete("links-tags:1/1");
        assertThat(redisTemplate.opsForValue().get("links:1")).isNull();
        assertThat(redisTemplate.opsForValue().get("links-tags:1/1")).isNull();
        assertThat(redisTemplate.opsForValue().get("links-tags:1/2")).isEqualTo(response);
    }

    @Test
    public void untrackLinkInvalidateTest() {
        ListLinkResponse expectedResult =
                new ListLinkResponse(List.of(new LinkResponse(1L, "link", List.of("1"), List.of())), 1);
        ListLinkResponse response =
                new ListLinkResponse(List.of(new LinkResponse(1L, "link2", List.of("2"), List.of())), 1);
        redisTemplate.opsForValue().set("links:1", expectedResult);
        redisTemplate.opsForValue().set("links-tags:1/1", expectedResult);
        redisTemplate.opsForValue().set("links-tags:1/2", response);
        stubFor(delete(urlPathMatching("/links*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("{\"id\": 1, \"url\": \"link\", \"tags\": [\"1\"], \"filters\": []}")));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        scrapperClient.untrackLink(1, new RemoveLinkRequest("link"));

        verify(redisTemplate, times(1)).delete("links:1");
        verify(redisTemplate, times(1)).delete("links-tags:1/1");
        assertThat(redisTemplate.opsForValue().get("links:1")).isNull();
        assertThat(redisTemplate.opsForValue().get("links-tags:1/1")).isNull();
        assertThat(redisTemplate.opsForValue().get("links-tags:1/2")).isEqualTo(response);
    }

    @Test
    public void getLinksByTagFromCacheTest() {
        ListLinkResponse expectedResult =
                new ListLinkResponse(List.of(new LinkResponse(1L, "link", List.of(), List.of())), 1);
        redisTemplate.opsForValue().set("links-tags:1/1", expectedResult);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        ListLinkResponse actualResult = scrapperClient.getLinksByTag(1L, "1");

        assertThat(actualResult).isEqualTo(expectedResult);
        verify(redisTemplate.opsForValue(), times(1)).get("links-tags:1/1");
        verify(redisTemplate.opsForValue(), times(0)).set(anyString(), any());
    }

    @Test
    public void setLinksByTagToCacheTest() {
        ListLinkResponse expectedResult = new ListLinkResponse(List.of(), 0);
        stubFor(get(urlPathMatching("/links*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("{\"links\":[], \"size\": 0}")));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        ListLinkResponse actualResult = scrapperClient.getLinksByTag(1L, "1");

        assertThat(actualResult).isEqualTo(expectedResult);
        verify(redisTemplate.opsForValue(), times(1)).set(eq("links-tags:1/1"), eq(expectedResult), any());
    }

    @Test
    public void handle4xxStatusTest() {
        int expectedStatus = 400;
        String expectedUrl = "http://localhost:8080/links?Tg-Chat-Id=1";
        StringBuilder sb = new StringBuilder();
        sb.append("{").append("\"description\": ").append("\"example description\",");
        sb.append("\"exceptionName\": ").append("\"Example\",");
        sb.append("\"exceptionMessage\": ").append("\"example message\",");
        sb.append("\"code\": ").append("\"BAD_REQUEST\"").append(",");
        sb.append("\"stacktrace\": ").append("[]}");
        stubFor(get(urlPathMatching("/links*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(400)
                        .withBody(sb.toString())));

        assertThatExceptionOfType(ApiCallException.class)
                .isThrownBy(() -> scrapperClient.getLinks(1))
                .satisfies(ex -> {
                    assertThat(ex.description()).isEqualTo("example description");
                    assertThat(ex.getMessage()).isEqualTo("example message");
                    assertThat(ex.url()).isEqualTo(expectedUrl);
                    assertThat(ex.code()).isEqualTo(expectedStatus);
                });
    }

    @Test
    public void handle5xxStatusTest() {
        int expectedStatus = 500;
        String expectedUrl = "http://localhost:8080/links?Tg-Chat-Id=1";
        StringBuilder sb = new StringBuilder();
        sb.append("{").append("\"description\": ").append("\"example description\",");
        sb.append("\"exceptionName\": ").append("\"Example\",");
        sb.append("\"exceptionMessage\": ").append("\"example message\",");
        sb.append("\"code\": ").append("\"INTERNAL_SERVER_ERROR\"").append(",");
        sb.append("\"stacktrace\": ").append("[]}");
        stubFor(get(urlPathMatching("/links*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(400)
                        .withBody(sb.toString())));

        assertThatExceptionOfType(ApiCallException.class)
                .isThrownBy(() -> scrapperClient.getLinks(1))
                .satisfies(ex -> {
                    assertThat(ex.description()).isEqualTo("Произошла ошибка");
                    assertThat(ex.getMessage()).isEqualTo("example message");
                    assertThat(ex.url()).isEqualTo(expectedUrl);
                    assertThat(ex.code()).isEqualTo(expectedStatus);
                });
    }
}
