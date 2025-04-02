package backend.academy.scrapper.service;

import backend.academy.scrapper.repository.filters.FiltersRepository;
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.repository.linkdata.LinkDataRepository;
import backend.academy.scrapper.repository.tags.TagsRepository;
import backend.academy.scrapper.repository.tgchat.TgChatRepository;
import backend.academy.scrapper.service.sql.SqlLinksCheckerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
public class JdbcAutowireTest extends ScrapperContainers {

    private final FiltersRepository filtersRepository;

    private final LinkDataRepository linkDataRepository;

    private final LinkRepository linkRepository;

    private final TagsRepository tagsRepository;

    private final TgChatRepository tgChatRepository;

    private final LinkDataService linkDataService;

    private final SqlLinksCheckerService sqlUpdatesCheckerService;

    @Autowired
    public JdbcAutowireTest(
            @Qualifier("jdbcFiltersRepository") FiltersRepository filtersRepository,
            @Qualifier("jdbcLinkDataRepository") LinkDataRepository linkDataRepository,
            @Qualifier("jdbcLinkRepository") LinkRepository linkRepository,
            @Qualifier("jdbcTagsRepository") TagsRepository tagsRepository,
            @Qualifier("jdbcTgChatRepository") TgChatRepository tgChatRepository,
            @Qualifier("sqlLinkDataService") LinkDataService linkDataService,
            @Qualifier("sqlLinksCheckerService") SqlLinksCheckerService sqlUpdatesCheckerService) {
        this.filtersRepository = filtersRepository;
        this.linkDataRepository = linkDataRepository;
        this.linkRepository = linkRepository;
        this.tagsRepository = tagsRepository;
        this.tgChatRepository = tgChatRepository;
        this.linkDataService = linkDataService;
        this.sqlUpdatesCheckerService = sqlUpdatesCheckerService;
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("app.access-type", () -> "SQL");
    }

    @Test
    public void autowireTest() {}
}
