package com.example.bankcards.mapper;

import com.example.bankcards.dto.request.CardRequestDTO;
import com.example.bankcards.dto.response.CardResponseDTO;
import com.example.bankcards.entity.CardEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CardMapper {
    CardEntity toEntity(CardRequestDTO dto);
    @Mapping(source = "cardNumberEncrypted", target = "cardNumber")
    CardResponseDTO toDTO(CardEntity entity);
    @Mapping(source = "cardNumberEncrypted", target = "cardNumber")
    List<CardResponseDTO> toDTOList(List<CardEntity> entities);
}
