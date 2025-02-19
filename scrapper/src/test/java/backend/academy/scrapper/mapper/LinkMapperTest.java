package backend.academy.scrapper.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.shared.dto.AddLinkRequest;
import backend.academy.shared.dto.LinkResponse;
import backend.academy.scrapper.entity.LinkData;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

public class LinkMapperTest {

    private final LinkMapper linkMapper = new LinkMapper();

    @Test
    public void createLinkResponseTest() {
        LinkData linkData = new LinkData(1L, 1, 1, List.of("work"), List.of("user=user1"), LocalDateTime.now());
        String link = "string";

        LinkResponse actualResponse = linkMapper.createLinkResponse(linkData, link);

        assertThat(actualResponse.id()).isEqualTo(linkData.id());
        assertThat(actualResponse.url()).isEqualTo(link);
        assertThat(actualResponse.tags().size()).isEqualTo(linkData.tags().size());
        assertThat(actualResponse.filters().size()).isEqualTo(linkData.filters().size());
    }

    @Test
    public void createLinkDataTest() {
        AddLinkRequest request = new AddLinkRequest("string", List.of("work"), List.of("user=user1"));
        int chatId = 1;
        int linkId = 1;

        LinkData actualLinkData = linkMapper.createLinkData(request, chatId, linkId);

        assertThat(actualLinkData.id()).isEqualTo(null);
        assertThat(actualLinkData.linkId()).isEqualTo(linkId);
        assertThat(actualLinkData.chatId()).isEqualTo(chatId);
        assertThat(actualLinkData.tags().size()).isEqualTo(request.tags().size());
        assertThat(actualLinkData.filters().size()).isEqualTo(request.filters().size());
    }
}
