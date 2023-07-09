package com.ufanet.meetingsbot.mapper;

import com.ufanet.meetingsbot.dto.AccountDto;
import com.ufanet.meetingsbot.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import org.telegram.telegrambots.meta.api.objects.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccountMapper {

    AccountMapper MAPPER = Mappers.getMapper(AccountMapper.class);

    Account map(AccountDto dto);

    AccountDto map(Account entity);

    @Mapping(target = "firstname", source = "user.firstName")
    @Mapping(target = "lastname", source = "user.lastName")
    @Mapping(target = "username", source = "user.userName")
    @Mapping(target = "language", source = "user.languageCode")
    AccountDto mapToDtoFromTgUser(User user);

    @Mapping(target = "firstname", source = "user.firstName")
    @Mapping(target = "lastname", source = "user.lastName")
    @Mapping(target = "username", source = "user.userName")
    Account mapToEntityFromTgUser(User user);

    @Mapping(target = "zoneId", source = "entity.settings.zoneId")
    @Mapping(target = "language", source = "entity.settings.language")
    AccountDto mapWithSettings(Account entity);

    @Mapping(target = "settings.zoneId", source = "dto.zoneId")
    @Mapping(target = "settings.language", source = "dto.language")
    Account mapWithSettings(AccountDto dto);

}
