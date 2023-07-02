package com.ufanet.meetingsbot.mapper;

import com.ufanet.meetingsbot.dto.SubjectDto;
import com.ufanet.meetingsbot.model.Subject;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface SubjectMapper {
    SubjectDto map(Subject subject);

    @InheritInverseConfiguration
    Subject map(SubjectDto subjectDto);
}
