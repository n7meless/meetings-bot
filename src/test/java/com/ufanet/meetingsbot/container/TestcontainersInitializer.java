package com.ufanet.meetingsbot.container;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

public class TestcontainersInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private final static GenericContainer REDIS_CONTAINER =
            new GenericContainer(DockerImageName.parse("redis:7.0.11-alpine"))
                    .withExposedPorts(6379);
    private final static PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
            new PostgreSQLContainer<>("postgres:15.3-alpine")
                    .withDatabaseName("integration-db")
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
                "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
                "spring.datasource.url=jdbc:tc:postgresql://" + POSTGRESQL_CONTAINER.getDatabaseName(),
                "spring.datasource.username=" + POSTGRESQL_CONTAINER.getUsername(),
                "spring.datasource.password=" + POSTGRESQL_CONTAINER.getPassword()
        ).applyTo(applicationContext.getEnvironment());
    }
}

