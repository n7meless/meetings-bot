package com.ufanet.meetingsbot.telegram;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.MockRestServiceServerAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.telegram.telegrambots.meta.api.objects.User;

import java.io.IOException;

@RestClientTest(excludeAutoConfiguration = MockRestServiceServerAutoConfiguration.class)
public class TelegramBotApiTest {

    @Value("${telegram.bot.token}")
    private String botToken;
    @Value("${telegram.bot.username}")
    private String botUsername;

    @Autowired
    private ObjectMapper mapper;
    private TestRestTemplate restTemplate;

    @Before
    @Autowired
    public void init(RestTemplateBuilder builder) {
        this.restTemplate = new TestRestTemplate(builder.rootUri("https://api.telegram.org/bot" + botToken));
    }

    @Test
    public void shouldGetSuccessConnection() throws IOException {
        ResponseEntity<String> response = restTemplate.getForEntity("/getMe", String.class);

        JsonNode responseJson = mapper.readTree(response.getBody());
        User user = mapper.convertValue(responseJson.get("result"), User.class);

        Assertions.assertTrue(response.hasBody());
        Assertions.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assertions.assertTrue(user.getIsBot());
        Assertions.assertEquals(user.getUserName(), botUsername);
    }
}
