package com.ufanet.meetingsbot.mapper;

import com.ufanet.meetingsbot.dto.MeetingDto;
import com.ufanet.meetingsbot.model.Meeting;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

import java.util.Optional;
import java.util.function.Supplier;

public interface MeetingMapper {
    MeetingDto map(Meeting entity);

    @InheritInverseConfiguration
    Meeting map(MeetingDto meeting);

    MeetingDto mapIfPresentOrElseGet(Optional<Meeting> meeting,
                                            Supplier<? extends MeetingDto> supplier);

    <X extends Throwable> MeetingDto mapIfPresentOrElseThrow(Optional<Meeting> meeting,
                                                                    Supplier<? extends X> exceptionSupplier) throws X;
}
