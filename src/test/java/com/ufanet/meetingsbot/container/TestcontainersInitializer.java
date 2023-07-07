package com.ufanet.meetingsbot.container;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

@DirtiesContext
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application.properties")
public class TestcontainersInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    static GenericContainer REDIS_CONTAINER =
            new GenericContainer(DockerImageName.parse("redis:7.0.11-alpine"))
                    .withExposedPorts(6379);
    static PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
            new PostgreSQLContainer<>("postgres:15.3-alpine")
                    .withUsername("postgres")
                    .withPassword("postgres")
                    .withExposedPorts(5432)
                    .withReuse(true);

    static {
        Startables.deepStart(REDIS_CONTAINER, POSTGRESQL_CONTAINER).join();
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        TestPropertyValues.of(
                "spring.redis.host=" + REDIS_CONTAINER.getHost(),
                "spring.redis.port=" + REDIS_CONTAINER.getFirstMappedPort(),
                "spring.datasource.url=" + POSTGRESQL_CONTAINER.getJdbcUrl(),
                "spring.datasource.username=" + POSTGRESQL_CONTAINER.getUsername(),
                "spring.datasource.password=" + POSTGRESQL_CONTAINER.getPassword()
        ).applyTo(applicationContext.getEnvironment());
    }
}

