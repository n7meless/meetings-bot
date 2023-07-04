package com.ufanet.meetingsbot.mapper;

import com.ufanet.meetingsbot.dto.AccountTimeDto;
import com.ufanet.meetingsbot.entity.AccountTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccountTimeMapper {

    AccountTimeMapper MAPPER = Mappers.getMapper(AccountTimeMapper.class);

    @Mapping(target = "account", source = "entity.account")
    @Mapping(target = "account.id", source = "entity.account.id")
    @Mapping(target = "meetingTime.id", source = "entity.meetingTime.id")
    @Mapping(target = "meetingTime.dateTime", source = "entity.meetingTime.dateTime")
    @Mapping(target = "meetingTime.meetingDate.id", source = "entity.meetingTime.meetingDate.id")
    @Mapping(target = "meetingTime.accountTimes", ignore = true)
    @Mapping(target = "meetingTime.meetingDate.meeting", ignore = true)
    @Mapping(target = "meetingTime.meetingDate.meetingTimes", ignore = true)
    AccountTimeDto map(AccountTime entity);

    @Mapping(target = "account", source = "dto.account")
    @Mapping(target = "account.id", source = "dto.account.id")
    @Mapping(target = "meetingTime.id", source = "dto.meetingTime.id")
    @Mapping(target = "meetingTime.dateTime", source = "dto.meetingTime.dateTime")
    @Mapping(target = "meetingTime.meetingDate.id", source = "dto.meetingTime.meetingDate.id")
    @Mapping(target = "meetingTime.accountTimes", ignore = true)
    @Mapping(target = "meetingTime.meetingDate.meetingTimes", ignore = true)
    AccountTime map(AccountTimeDto dto);
}
