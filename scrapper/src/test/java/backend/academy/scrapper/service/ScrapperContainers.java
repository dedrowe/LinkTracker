package backend.academy.scrapper.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

@Testcontainers
public class ScrapperContainers {

    @ServiceConnection
    protected static final PostgreSQLContainer<?> postgres;

    @ServiceConnection
    protected static final KafkaContainer kafka = new KafkaContainer("apache/kafka-native:4.0.0");

    static {
        postgres = new PostgreSQLContainer<>("postgres:17-alpine");
        postgres.start();
        kafka.start();
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
}
