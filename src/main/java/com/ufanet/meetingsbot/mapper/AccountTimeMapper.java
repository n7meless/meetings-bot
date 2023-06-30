package com.ufanet.meetingsbot.mapper;

import com.ufanet.meetingsbot.dto.AccountTimeDto;
import com.ufanet.meetingsbot.model.AccountTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountTimeMapper {
    @Mapping(target = "account", source = "entity.account")
    @Mapping(target = "account.id", source = "entity.account.id")
    @Mapping(target = "meetingTime", source = "entity.meetingTime")
    @Mapping(target = "meetingTime.accountTimes", ignore = true)
    AccountTimeDto map(AccountTime entity);

    @Mapping(target = "account", source = "dto.account")
    @Mapping(target = "account.id", source = "dto.account.id")
    @Mapping(target = "meetingTime.id", source = "dto.meetingTime.id")
    @Mapping(target = "meetingTime.accountTimes", ignore = true)
//    @Mapping(target = "meetingTime.id", source = "entity.meetingTime.id")
    AccountTime map(AccountTimeDto dto);
}
