package com.ufanet.meetingsbot.mapper;

import com.ufanet.meetingsbot.dto.GroupDto;
import com.ufanet.meetingsbot.model.Group;
import org.mapstruct.Mapper;
import org.telegram.telegrambots.meta.api.objects.Chat;

@Mapper(componentModel = "spring")
public interface GroupMapper {
    Group map(GroupDto dto);

    GroupDto map(Group entity);
    Group map(Chat chat);
}
