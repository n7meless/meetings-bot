package com.ufanet.meetingsbot.mapper;

import com.ufanet.meetingsbot.dto.AccountMeetingDto;
import com.ufanet.meetingsbot.model.AccountMeeting;
import org.mapstruct.*;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccountMeetingMapper {
    @Mapping(target = "account", source = "entity.account")
    @Mapping(target = "account.timeZone", source = "entity.account.settings.timeZone")
    @Mapping(target = "account.language", source = "entity.account.settings.language")
    AccountMeetingDto map(AccountMeeting entity);

    @InheritInverseConfiguration
    @Mapping(target = "account", source = "dto.account")
    AccountMeeting map(AccountMeetingDto dto);
}