package backend.academy.scrapper.repository.jdbc;

import backend.academy.scrapper.service.ScrapperContainers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@JdbcTest
public abstract class AbstractJdbcTest extends ScrapperContainers {

    protected final JdbcClient client;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("app.access-type", () -> "SQL");
    }

    @Autowired
    public AbstractJdbcTest(JdbcClient client) {
        this.client = client;
    }
}
