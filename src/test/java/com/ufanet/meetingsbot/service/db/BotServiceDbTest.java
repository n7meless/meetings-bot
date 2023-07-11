package com.ufanet.meetingsbot.service.db;

import com.ufanet.meetingsbot.annotation.DatabaseTest;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.constants.type.MessageType;
import com.ufanet.meetingsbot.entity.Account;
import com.ufanet.meetingsbot.entity.BotState;
import com.ufanet.meetingsbot.service.BotService;
import com.ufanet.meetingsbot.service.ServiceTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DatabaseTest
@ContextConfiguration(classes = ServiceTestConfiguration.class)
public class BotServiceDbTest {

    @Autowired
    private BotService botService;

    @Test
    void shouldReturnBotState_whenGetByAccountId() {
        BotState saved = botService.getByUserId(1);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertNotNull(saved.getState());
        assertNotNull(saved.getMessageId());
        assertNotNull(saved.getMessageType());
    }

    @Test
    void shouldReturnBotState_whenSaveInDatabase() {
        Account account = Account.builder().id(123L).build();

        BotState botState = BotState.builder().updatedDt(LocalDateTime.now())
                .messageId(1234).messageType(MessageType.SEND_MESSAGE)
                .account(account).state(MeetingState.GROUP_SELECT.name()).build();

        BotState saved = botService.save(botState);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals(botState.getState(), saved.getState());
        assertEquals(botState.getMessageId(), saved.getMessageId());
        assertEquals(botState.getMessageType(), saved.getMessageType());
    }
}
