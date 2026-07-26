package com.example.bankcards.mapper;

import com.example.bankcards.dto.request.CardRequestDTO;
import com.example.bankcards.entity.CardEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CardMapper {
    CardEntity toEntity(CardRequestDTO dto);
}
