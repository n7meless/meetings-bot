package com.ufanet.meetingsbot.annotation;

import com.ufanet.meetingsbot.container.TestcontainersInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ContextConfiguration(initializers = TestcontainersInitializer.class)
@TestPropertySource("classpath:application-testcontainers.properties")
public @interface EnableTestcontainers {
}
