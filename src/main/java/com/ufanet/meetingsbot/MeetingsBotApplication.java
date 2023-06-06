package com.ufanet.meetingsbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class MeetingsBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeetingsBotApplication.class, args);
    }
}
