package backend.academy.scrapper.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.entity.TgChat;
import backend.academy.shared.dto.LinkResponse;
import java.util.List;
import org.junit.jupiter.api.Test;

public class LinkMapperTest {

    private final LinkMapper linkMapper = new LinkMapper();

    @Test
    public void createLinkResponseTest() {
        LinkData linkData = new LinkData(1L, new Link(), new TgChat());
        String link = "string";
        List<String> expectedTags = List.of("string");
        List<String> expectedFilters = List.of("string");

        LinkResponse actualResponse = linkMapper.createLinkResponse(linkData, link, expectedTags, expectedFilters);

        assertThat(actualResponse.id()).isEqualTo(linkData.id());
        assertThat(actualResponse.url()).isEqualTo(link);
        assertThat(actualResponse.tags().size()).isEqualTo(1);
        assertThat(actualResponse.tags().getFirst()).isEqualTo(expectedTags.getFirst());
        assertThat(actualResponse.filters().size()).isEqualTo(1);
        assertThat(actualResponse.filters().getFirst()).isEqualTo(expectedFilters.getFirst());
    }
}
