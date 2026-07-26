package com.example.bankcards.mapper;

import com.example.bankcards.dto.request.AccountCredentialsDTO;
import com.example.bankcards.entity.AccountCredentialsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountCredentialsMapper {
    @Mapping(source = "password", target = "passwordHashed")
    AccountCredentialsEntity toEntity(AccountCredentialsDTO dto);
}
