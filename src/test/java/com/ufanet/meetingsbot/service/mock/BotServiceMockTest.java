package com.ufanet.meetingsbot.service.mock;

import com.ufanet.meetingsbot.cache.impl.BotStateCache;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.entity.BotState;
import com.ufanet.meetingsbot.repository.BotRepository;
import com.ufanet.meetingsbot.service.BotService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class BotServiceMockTest {
    @Mock
    private BotRepository botRepository;
    @Mock
    private BotStateCache botStateCache;
    @InjectMocks
    private BotService botService;

    private BotState dummyBotState(long id) {
        return BotState.builder()
                .state(MeetingState.PARTICIPANT_SELECT.name())
                .msgFromBot(true).messageId(12345)
                .build();
    }

    @Test
    public void shouldReturnBotStateWhenGetByAccountId() {
        //given
        BotState dummyBotState = dummyBotState(1L);

        //when + then
        Mockito.when(botRepository.findByAccountId(Mockito.anyLong())).thenReturn(Optional.ofNullable(dummyBotState));
        BotState botState = botService.getByUserId(Mockito.anyLong());

        Assertions.assertEquals(dummyBotState.getId(), botState.getId());
        Assertions.assertEquals(dummyBotState.getMessageId(), botState.getMessageId());
        Assertions.assertEquals(dummyBotState.getState(), botState.getState());
        Assertions.assertEquals(dummyBotState.isMsgFromBot(), botState.isMsgFromBot());
    }

    @Test
    public void shouldReturnSavedBotState() {
        //given
        BotState dummyBotState = dummyBotState(1L);

        //when + then
        Mockito.when(botRepository.save(Mockito.any(BotState.class))).thenReturn(dummyBotState);
        BotState botState = botService.save(dummyBotState);

        Assertions.assertNotNull(botState);
        Assertions.assertEquals(dummyBotState.getId(), botState.getId());
        Assertions.assertEquals(dummyBotState.getMessageId(), botState.getMessageId());
        Assertions.assertEquals(dummyBotState.getState(), botState.getState());
        Assertions.assertEquals(dummyBotState.isMsgFromBot(), botState.isMsgFromBot());
    }

    @Test
    public void shouldSaveAllBotStates() {
        //given
        List<BotState> dummyBotStates = new ArrayList<>();
        for (long i = 0; i < 5; i++) {
            BotState dummyBotState = dummyBotState(i);
            dummyBotStates.add(dummyBotState);
        }
        //when + then
        Mockito.when(botRepository.saveAll(dummyBotStates)).thenReturn(dummyBotStates);
        List<BotState> savedBotStates = botService.saveAll(dummyBotStates);

        Assertions.assertNotNull(savedBotStates);
        Assertions.assertSame(savedBotStates.size(), dummyBotStates.size());
        Assertions.assertEquals(savedBotStates, dummyBotStates);
    }
}
