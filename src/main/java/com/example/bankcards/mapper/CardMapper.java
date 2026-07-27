package com.example.bankcards.mapper;

import com.example.bankcards.dto.request.CardRequestDTO;
import com.example.bankcards.dto.response.CardResponseDTO;
import com.example.bankcards.entity.CardEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CardMapper {
    CardEntity toEntity(CardRequestDTO dto);
    CardResponseDTO toDTO(CardEntity entity);
    List<CardResponseDTO> toDTOList(List<CardEntity> entities);
}
