package com.ufanet.meetingsbot.annotation;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@Retention(RetentionPolicy.RUNTIME)
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@EnableTestcontainers
@Tag("integration-test")
@Sql(scripts = "classpath:sql/init-database.sql")
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
public @interface DatabaseTest {
}

