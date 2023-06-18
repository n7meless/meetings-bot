package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.constants.state.AccountState;
import com.ufanet.meetingsbot.model.BotState;
import com.ufanet.meetingsbot.repository.BotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BotService {
    private final AccountService accountService;
    private final BotRepository botRepository;
    public int getLastMessageId(long userId){
        BotState botState = getByUserId(userId);
        return botState.getMessageId();
    }
    public BotState getByUserId(long userId){
        return botRepository.findByAccountId(userId).orElseThrow();
    }
    public void save(BotState botState){
        botRepository.save(botState);
    }
}
