package com.example.bankcards.mapper;

import com.example.bankcards.dto.request.AccountRequestDTO;
import com.example.bankcards.dto.response.AccountResponseDTO;
import com.example.bankcards.entity.AccountEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    AccountEntity toEntity(AccountRequestDTO dto);
    List<AccountResponseDTO> toDTOList(List<AccountEntity> entities);
}
