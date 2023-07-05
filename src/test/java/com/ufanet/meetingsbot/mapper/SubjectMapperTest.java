package com.ufanet.meetingsbot.mapper;

import com.ufanet.meetingsbot.dto.SubjectDto;
import com.ufanet.meetingsbot.entity.Subject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SubjectMapperTest {
    @Test
    void shouldReturnSubjectDtoWhenMapsFromEntity() {
        //given
        Subject entity = Subject.builder()
                .id(10L).title("title")
                .duration(100)
                .build();

        //when
        SubjectDto dto = SubjectMapper.MAPPER.map(entity);

        //then
        Assertions.assertEquals(dto.getId(), entity.getId());
        Assertions.assertEquals(dto.getTitle(), entity.getTitle());
        Assertions.assertEquals(dto.getDuration(), entity.getDuration());
    }

    @Test
    void shouldReturnSubjectEntityWhenMapsFromDto() {
        //given
        SubjectDto dto = SubjectDto.builder()
                .id(10L).title("title")
                .duration(100)
                .build();

        //when
        Subject entity = SubjectMapper.MAPPER.map(dto);

        //then
        Assertions.assertEquals(dto.getId(), entity.getId());
        Assertions.assertEquals(dto.getTitle(), entity.getTitle());
        Assertions.assertEquals(dto.getDuration(), entity.getDuration());
    }
}
