package com.ufanet.meetingsbot.mapper;

import com.ufanet.meetingsbot.dto.SubjectDto;
import com.ufanet.meetingsbot.model.Subject;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubjectMapper {
    SubjectDto map(Subject subject);

    @InheritInverseConfiguration
    Subject map(SubjectDto subjectDto);
}
