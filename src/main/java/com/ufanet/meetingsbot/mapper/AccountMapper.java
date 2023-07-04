package com.ufanet.meetingsbot.mapper;

import com.ufanet.meetingsbot.dto.AccountDto;
import com.ufanet.meetingsbot.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccountMapper {

    AccountMapper MAPPER = Mappers.getMapper(AccountMapper.class);

    @Mapping(target = "settings.timeZone", source = "dto.timeZone")
    @Mapping(target = "settings.language", source = "dto.language")
    Account map(AccountDto dto);

    @Mapping(target = "timeZone", source = "entity.settings.timeZone")
    @Mapping(target = "language", source = "entity.settings.language")
    AccountDto map(Account entity);
}
