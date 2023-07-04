package com.ufanet.meetingsbot.mapper;

import com.ufanet.meetingsbot.dto.MeetingTimeDto;
import com.ufanet.meetingsbot.entity.MeetingTime;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MeetingTimeMapper {
    MeetingTimeMapper MAPPER = Mappers.getMapper(MeetingTimeMapper.class);

    @Mapping(target = "meetingDate.id", source = "entity.meetingDate.id")
    @Mapping(target = "meetingDate.date", source = "entity.meetingDate.date")
    @Mapping(target = "meetingDate.meetingTimes", ignore = true)
    @Mapping(target = "meetingDate.meeting", ignore = true)
    @Mapping(target = "accountTimes", ignore = true)
    MeetingTimeDto map(MeetingTime entity);

    @InheritInverseConfiguration
    @Mapping(target = "meetingDate.id", source = "dto.meetingDate.id")
    @Mapping(target = "meetingDate.date", source = "dto.meetingDate.date")
    @Mapping(target = "meetingDate.meetingTimes", ignore = true)
    @Mapping(target = "meetingDate.meeting", ignore = true)
    @Mapping(target = "accountTimes", ignore = true)
    MeetingTime map(MeetingTimeDto dto);
}
