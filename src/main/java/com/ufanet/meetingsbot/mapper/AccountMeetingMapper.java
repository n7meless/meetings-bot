package com.ufanet.meetingsbot.mapper;

import com.ufanet.meetingsbot.dto.AccountMeetingDto;
import com.ufanet.meetingsbot.dto.SubjectDto;
import com.ufanet.meetingsbot.model.AccountMeeting;
import com.ufanet.meetingsbot.model.Subject;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface AccountMeetingMapper {
    @Mapping(target = "account", source = "entity.account")
    @Mapping(target = "account.timeZone", source = "entity.account.settings.timeZone")
    @Mapping(target = "account.language", source = "entity.account.settings.language")
    AccountMeetingDto map(AccountMeeting entity);

    @InheritInverseConfiguration
    @Mapping(target = "account", source = "dto.account")
    AccountMeeting map(AccountMeetingDto dto);
}