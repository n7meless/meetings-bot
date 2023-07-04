package com.ufanet.meetingsbot.mapper;

import com.ufanet.meetingsbot.dto.SubjectDto;
import com.ufanet.meetingsbot.entity.Subject;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubjectMapper {

    SubjectMapper MAPPER = Mappers.getMapper(SubjectMapper.class);


    SubjectDto map(Subject entity);

    @InheritInverseConfiguration
    Subject map(SubjectDto dto);
}
