package backend.academy.scrapper.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkData;
import backend.academy.scrapper.entity.TgChat;
import backend.academy.scrapper.entity.jpa.JpaLinkData;
import backend.academy.scrapper.service.entityFactory.linkData.LinkDataFactory;
import backend.academy.shared.dto.LinkResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LinkMapperTest {

    @Mock
    private LinkDataFactory linkDataFactory;

    @InjectMocks
    private LinkMapper linkMapper;

    @Test
    public void createLinkResponseTest() {
        LinkData linkData = new JpaLinkData(1L, new Link(), new TgChat());
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
