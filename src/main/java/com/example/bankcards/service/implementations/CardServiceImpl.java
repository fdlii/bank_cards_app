package com.example.bankcards.service.implementations;

import com.example.bankcards.entity.*;
import com.example.bankcards.exception.account.AccountNotFoundException;
import com.example.bankcards.exception.card.BlockRequestNotFoundException;
import com.example.bankcards.exception.card.CardNotFoundException;
import com.example.bankcards.exception.card.InsufficientFundsException;
import com.example.bankcards.exception.card.InvalidCardStatusException;
import com.example.bankcards.repository.AccountRepository;
import com.example.bankcards.repository.CardBlockRequestRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.interfaces.CardService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CardServiceImpl implements CardService {
    private final String BIN = "541234";

    private final AccountRepository accountRepo;
    private final CardRepository cardRepo;
    private final CardBlockRequestRepository blockRequestRepo;

    public CardServiceImpl(AccountRepository accountRepo, CardRepository cardRepo, CardBlockRequestRepository blockRequestRepo) {
        this.accountRepo = accountRepo;
        this.cardRepo = cardRepo;
        this.blockRequestRepo = blockRequestRepo;
    }

    @Override
    public List<CardEntity> getAllCards(String firstName, String lastName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<CardEntity> cardEntities = cardRepo.findAllCardsWithFilters(firstName, lastName, pageable);
        cardEntities.forEach(c -> c.setCardNumberEncrypted("**** **** **** " + c.getLastFourDigits()));
        return cardEntities;
    }

    @Override
    @Transactional
    public void createCard(CardEntity entity, long accountId) {
        AccountEntity account = accountRepo.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("No account with id " + accountId));
        entity.setAccount(account);

        entity.setCardNumberEncrypted("temp");
        entity.setStatus(CardStatus.NEW);
        entity.setAccount(account);
        CardEntity saved = cardRepo.save(entity);

        String number = String.format("%010d", saved.getId());
        String finalNumber = BIN + number;

        saved.setLastFourDigits(finalNumber.substring(12));
        saved.setCardNumberEncrypted(finalNumber);
    }

    @Override
    @Transactional
    public void activateCard(long id) {
        CardEntity entity = cardRepo.findById(id).orElseThrow(() -> new CardNotFoundException("No card with id " + id));
        entity.setStatus(CardStatus.ACTIVE);
    }

    @Override
    @Transactional
    public void deleteCard(long id) {
        CardEntity entity = cardRepo.findById(id).orElseThrow(() -> new CardNotFoundException("No card with id " + id));
        cardRepo.delete(entity);
    }

    @Override
    public List<CardBlockRequestEntity> getActiveBlockRequests() {
        return blockRequestRepo.getActiveBlockRequests();
    }

    @Override
    @Transactional
    public String approveBlockRequest(long id) {
        CardBlockRequestEntity entity = blockRequestRepo.findById(id)
                .orElseThrow(() -> new BlockRequestNotFoundException("No block request with id " + id));
        entity.setStatus(BlockRequestStatus.APPROVED);

        CardEntity cardEntity = entity.getCard();
        cardEntity.setStatus(CardStatus.BLOCKED);

        return "**** **** **** " + cardEntity.getLastFourDigits();
    }

    @Override
    @Transactional
    public void rejectBlockRequest(long id) {
        CardBlockRequestEntity entity = blockRequestRepo.findById(id)
                .orElseThrow(() -> new BlockRequestNotFoundException("No block request with id " + id));
        entity.setStatus(BlockRequestStatus.REJECTED);
    }



    @Override
    @Transactional
    public List<CardEntity> getUserCards(int page, int size) {
        AccountCredentialsEntity credentials =
                (AccountCredentialsEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AccountEntity entity = accountRepo.findByCredentialsIdWithCards(credentials.getId());
        return entity.getCards().stream().skip((long) page * size).limit(size).toList();
    }

    @Override
    @Transactional
    public CardEntity getCardByNumber(String number) {
        AccountCredentialsEntity credentials =
                (AccountCredentialsEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AccountEntity entity = accountRepo.findByCredentialsIdWithCards(credentials.getId());
        CardEntity cardEntity = entity.getCards().stream()
                .filter(c -> c.getCardNumberEncrypted().equals(number)).findFirst()
                .orElseThrow(() -> new CardNotFoundException("No card with number " + number));
        return cardEntity;
    }

    @Override
    @Transactional
    public void createBlockRequest(String number) {
        AccountCredentialsEntity credentials =
                (AccountCredentialsEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AccountEntity entity = accountRepo.findByCredentialsIdWithCards(credentials.getId());
        CardEntity cardEntity = entity.getCards().stream()
                .filter(c -> c.getCardNumberEncrypted().equals(number)).findFirst()
                .orElseThrow(() -> new CardNotFoundException("No card with number " + number));
        CardBlockRequestEntity blockRequestEntity = CardBlockRequestEntity.builder()
                .card(cardEntity)
                .account(entity)
                .status(BlockRequestStatus.PENDING)
                .build();
        blockRequestRepo.save(blockRequestEntity);
    }

    @Override
    @Transactional
    public void transferMoney(String from, String to, double sum) {
        AccountCredentialsEntity credentials =
                (AccountCredentialsEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AccountEntity entity = accountRepo.findByCredentialsIdWithCards(credentials.getId());
        CardEntity cardFrom = entity.getCards().stream()
                .filter(c -> c.getCardNumberEncrypted().equals(from)).findFirst()
                .orElseThrow(() -> new CardNotFoundException("No card with number " + from));
        CardEntity cardTo = entity.getCards().stream()
                .filter(c -> c.getCardNumberEncrypted().equals(to)).findFirst()
                .orElseThrow(() -> new CardNotFoundException("No card with number " + to));

        if (cardFrom.getStatus() != CardStatus.ACTIVE || cardTo.getStatus() != CardStatus.ACTIVE) {
            throw new InvalidCardStatusException("At least one of the cards is not activated.");
        }
        if (cardFrom.getBalance() < sum) {
            throw new InsufficientFundsException("Insufficient funds on card with number " + from);
        }

        cardFrom.setBalance(cardFrom.getBalance() - sum);
        cardTo.setBalance(cardTo.getBalance() + sum);
    }
}