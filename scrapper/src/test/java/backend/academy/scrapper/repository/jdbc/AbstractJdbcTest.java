package backend.academy.scrapper.repository.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@JdbcTest
public abstract class AbstractJdbcTest {

    @ServiceConnection
    protected static final PostgreSQLContainer<?> postgres;

    protected final JdbcClient client;

    static {
        postgres = new PostgreSQLContainer<>("postgres:15");
        postgres.start();
        try (Connection connection =
                DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())) {
            Database database =
                    DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            Liquibase liquibase = new Liquibase("/migrations/master.xml", new ClassLoaderResourceAccessor(), database);
            liquibase.dropAll();
            liquibase.update();
        } catch (LiquibaseException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Autowired
    public AbstractJdbcTest(JdbcClient client) {
        this.client = client;
    }
}
