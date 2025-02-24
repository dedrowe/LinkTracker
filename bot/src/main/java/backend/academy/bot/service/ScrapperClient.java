package backend.academy.bot.service;

import backend.academy.bot.BotConfig;
import backend.academy.shared.dto.AddLinkRequest;
import backend.academy.shared.dto.ApiErrorResponse;
import backend.academy.shared.dto.ListLinkResponse;
import backend.academy.shared.dto.RemoveLinkRequest;
import backend.academy.shared.exceptions.ApiCallException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class ScrapperClient {

    private final RestClient client;

    private final ObjectMapper mapper;

    @Autowired
    public ScrapperClient(BotConfig config) {
        client = RestClient.create(config.scrapper().url());
        mapper = new ObjectMapper();
    }

    public ScrapperClient(RestClient client, ObjectMapper mapper) {
        this.client = client;
        this.mapper = mapper;
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
        setStatusHandler(client.post().uri("/tg-chat/{id}", chatId).retrieve()).toBodilessEntity();
    }

    public void deleteChat(long chatId) {
        setStatusHandler(client.delete().uri("/tg-chat/{id}", chatId).retrieve())
                .toBodilessEntity();
    }

    public ListLinkResponse getLinks(long chatId) {
        return setStatusHandler(client.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/links")
                                .queryParam("Tg-Chat-Id", chatId)
                                .build())
                        .retrieve())
                .toEntity(ListLinkResponse.class)
                .getBody();
    }

    public void trackLink(long chatId, AddLinkRequest request) {
        setStatusHandler(client.post()
                        .uri(uriBuilder -> uriBuilder
                                .path("/links")
                                .queryParam("Tg-Chat-Id", chatId)
                                .build())
                        .body(request)
                        .retrieve())
                .toBodilessEntity();
    }

    public void untrackLink(long chatId, RemoveLinkRequest request) {
        setStatusHandler(client.method(HttpMethod.DELETE)
                        .uri(uriBuilder -> uriBuilder
                                .path("/links")
                                .queryParam("Tg-Chat-Id", chatId)
                                .build())
                        .body(request)
                        .retrieve())
                .toBodilessEntity();
    }
}
