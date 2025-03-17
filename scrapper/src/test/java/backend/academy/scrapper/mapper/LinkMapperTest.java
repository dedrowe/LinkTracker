package backend.academy.scrapper.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.entity.Filter;
import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.entity.Tag;
import backend.academy.shared.dto.LinkResponse;
import java.util.List;
import org.junit.jupiter.api.Test;

public class LinkMapperTest {

    private final LinkMapper linkMapper = new LinkMapper();

    @Test
    public void createLinkResponseTest() {
        LinkData linkData = new LinkData(1L, 1, 1);
        String link = "string";
        List<Tag> expectedTags = List.of(new Tag(1L, "string"));
        List<Filter> expectedFilters = List.of(new Filter(1L, "string"));

        LinkResponse actualResponse = linkMapper.createLinkResponse(linkData, link, expectedTags, expectedFilters);

        assertThat(actualResponse.id()).isEqualTo(linkData.id());
        assertThat(actualResponse.url()).isEqualTo(link);
        assertThat(actualResponse.tags().size()).isEqualTo(1);
        assertThat(actualResponse.tags().getFirst())
                .isEqualTo(expectedTags.getFirst().tag());
        assertThat(actualResponse.filters().size()).isEqualTo(1);
        assertThat(actualResponse.filters().getFirst())
                .isEqualTo(expectedFilters.getFirst().filter());
    }

    @Test
    public void createLinkDataTest() {
        int chatId = 1;
        int linkId = 1;

        LinkData actualLinkData = linkMapper.createLinkData(chatId, linkId);

        assertThat(actualLinkData.id()).isEqualTo(null);
        assertThat(actualLinkData.linkId()).isEqualTo(linkId);
        assertThat(actualLinkData.chatId()).isEqualTo(chatId);
    }
}
