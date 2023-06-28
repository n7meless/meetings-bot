package com.ufanet.meetingsbot.mapper;

import com.ufanet.meetingsbot.dto.AccountDto;
import com.ufanet.meetingsbot.model.Account;
import com.ufanet.meetingsbot.model.Settings;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {
    public AccountDto mapToDto(Account account) {
        AccountDto accountDto = new AccountDto();
        Settings settings = account.getSettings();
        if (settings != null){
            accountDto.setTimeZone(settings.getTimeZone());
            accountDto.setLanguage(settings.getLanguage());
        }
        accountDto.setFirstname(account.getFirstname());
        accountDto.setLastname(account.getLastname());
        accountDto.setId(account.getId());
        return accountDto;
    }
}
