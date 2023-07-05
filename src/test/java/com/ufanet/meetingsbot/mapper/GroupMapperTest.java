package com.ufanet.meetingsbot.mapper;

import com.ufanet.meetingsbot.dto.GroupDto;
import com.ufanet.meetingsbot.entity.Group;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class GroupMapperTest {

    @Test
    void shouldReturnGroupDtoWhenMapsFromEntity() {
        //given
        Group entity = Group.builder()
                .id(10L).description("description")
                .title("title").createdDt(LocalDateTime.now())
                .build();

        //when
        GroupDto dto = GroupMapper.MAPPER.map(entity);

        //then
        Assertions.assertEquals(dto.getId(), entity.getId());
        Assertions.assertEquals(dto.getTitle(), entity.getTitle());
        Assertions.assertEquals(dto.getDescription(), entity.getDescription());
        Assertions.assertEquals(dto.getCreatedDt(), entity.getCreatedDt());
    }

    @Test
    void shouldReturnGroupEntityWhenMapsFromDto() {
        //given
        GroupDto dto = GroupDto.builder()
                .id(10L).description("description")
                .title("title").createdDt(LocalDateTime.now())
                .build();

        //when
        Group entity = GroupMapper.MAPPER.map(dto);

        //then
        Assertions.assertEquals(dto.getId(), entity.getId());
        Assertions.assertEquals(dto.getTitle(), entity.getTitle());
        Assertions.assertEquals(dto.getDescription(), entity.getDescription());
        Assertions.assertEquals(dto.getCreatedDt(), entity.getCreatedDt());
    }
}
