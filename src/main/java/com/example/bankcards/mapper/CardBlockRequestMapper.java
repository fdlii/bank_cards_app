package com.example.bankcards.mapper;

import com.example.bankcards.dto.response.CardBlockReqDTO;
import com.example.bankcards.entity.CardBlockRequestEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CardBlockRequestMapper {
    List<CardBlockReqDTO> toDTOList(List<CardBlockRequestEntity> entities);
}
