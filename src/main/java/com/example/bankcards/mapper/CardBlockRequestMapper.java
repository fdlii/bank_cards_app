package com.example.bankcards.mapper;

import com.example.bankcards.dto.response.CardBlockReqDTO;
import com.example.bankcards.entity.CardBlockRequestEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CardBlockRequestMapper {
    @Mapping(source = "card.id", target = "cardId")
    @Mapping(source = "account.id", target = "accountId")
    CardBlockReqDTO toDTO(CardBlockRequestEntity entity);

    List<CardBlockReqDTO> toDTOList(List<CardBlockRequestEntity> entities);
}
