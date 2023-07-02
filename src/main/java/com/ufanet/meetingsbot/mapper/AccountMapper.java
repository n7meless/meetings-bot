package com.ufanet.meetingsbot.mapper;

import com.ufanet.meetingsbot.dto.AccountDto;
import com.ufanet.meetingsbot.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    @Mapping(target = "settings.timeZone", source = "dto.timeZone")
    @Mapping(target = "settings.language", source = "dto.language")
    Account map(AccountDto dto);

    @Mapping(target = "timeZone", source = "entity.settings.timeZone")
    @Mapping(target = "language", source = "entity.settings.language")
    AccountDto map(Account entity);
}
