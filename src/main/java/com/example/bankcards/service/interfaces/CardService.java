package com.example.bankcards.service.interfaces;

import com.example.bankcards.entity.CardBlockRequestEntity;
import com.example.bankcards.entity.CardEntity;

import java.util.List;

public interface CardService {
    List<CardEntity> getAllCards();
    void createCard(CardEntity entity, long accountId);
    void activateCard(long id);
    void deleteCard(long id);
    List<CardBlockRequestEntity> getActiveBlockRequests();
    String approveBlockRequest(long id);
    void rejectBlockRequest(long id);

    List<CardEntity> getUserCards();
    CardEntity getCardByNumber(String number);
    void createBlockRequest(String number);
    void transferMoney(String from, String to, double sum);
}
