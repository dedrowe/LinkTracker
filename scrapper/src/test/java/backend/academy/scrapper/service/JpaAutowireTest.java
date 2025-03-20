package backend.academy.scrapper.service;

import backend.academy.scrapper.repository.filters.FiltersRepository;
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.repository.linkdata.LinkDataRepository;
import backend.academy.scrapper.repository.tags.TagsRepository;
import backend.academy.scrapper.repository.tgchat.TgChatRepository;
import backend.academy.scrapper.service.sql.SqlUpdatesCheckerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
public class JpaAutowireTest {

    private final FiltersRepository filtersRepository;

    private final LinkDataRepository linkDataRepository;

    private final LinkRepository linkRepository;

    private final TagsRepository tagsRepository;

    private final TgChatRepository tgChatRepository;

    private final LinkDataService linkDataService;

    private final SqlUpdatesCheckerService sqlUpdatesCheckerService;

    @Autowired
    public JpaAutowireTest(
            @Qualifier("jpaFiltersRepository") FiltersRepository filtersRepository,
            @Qualifier("jpaLinkDataRepository") LinkDataRepository linkDataRepository,
            @Qualifier("jpaLinkRepository") LinkRepository linkRepository,
            @Qualifier("jpaTagsRepository") TagsRepository tagsRepository,
            @Qualifier("jpaTgChatRepository") TgChatRepository tgChatRepository,
            @Qualifier("ormLinkDataService") LinkDataService linkDataService,
            @Qualifier("ormUpdatesCheckerService") SqlUpdatesCheckerService sqlUpdatesCheckerService) {
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
        registry.add("app.access-type", () -> "ORM");
    }

    @Test
    public void autowireTest() {}
}
