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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CardServiceImpl implements CardService {
    private final String BIN = "541234";

    private final Logger logger = LoggerFactory.getLogger(CardServiceImpl.class);
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
        logger.info("Getting all cards.");
        Pageable pageable = PageRequest.of(page, size);
        List<CardEntity> cardEntities = cardRepo.findAllCardsWithFilters(firstName, lastName, pageable);
        cardEntities.forEach(c -> c.setCardNumberEncrypted("**** **** **** " + c.getLastFourDigits()));
        logger.info("Cards were got successfully.");
        return cardEntities;
    }

    @Override
    @Transactional
    public void createCard(CardEntity entity, long accountId) {
        logger.info("Creating card to user with id {}", accountId);
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
        logger.info("Card with number {} was successfully created.", finalNumber);
    }

    @Override
    @Transactional
    public void activateCard(long id) {
        logger.info("Activating card with id {}", id);
        CardEntity entity = cardRepo.findById(id).orElseThrow(() -> new CardNotFoundException("No card with id " + id));
        entity.setStatus(CardStatus.ACTIVE);
        logger.info("Card with id {} was successfully activated.", id);
    }

    @Override
    @Transactional
    public void deleteCard(long id) {
        logger.info("Deleting card with id {}", id);
        CardEntity entity = cardRepo.findById(id).orElseThrow(() -> new CardNotFoundException("No card with id " + id));
        cardRepo.delete(entity);
        logger.info("Card with id {} was successfully deleted.", id);
    }

    @Override
    public List<CardBlockRequestEntity> getActiveBlockRequests() {
        logger.info("Getting all active block requests.");
        List<CardBlockRequestEntity> requests = blockRequestRepo.getActiveBlockRequests();
        logger.info("Block requests were got successfully.");
        return requests;
    }

    @Override
    @Transactional
    public String approveBlockRequest(long id) {
        logger.info("Approving block request with id {}", id);
        CardBlockRequestEntity entity = blockRequestRepo.findById(id)
                .orElseThrow(() -> new BlockRequestNotFoundException("No block request with id " + id));
        entity.setStatus(BlockRequestStatus.APPROVED);

        CardEntity cardEntity = entity.getCard();
        cardEntity.setStatus(CardStatus.BLOCKED);

        logger.info("Block request with id {} was successfully approved.", id);
        return "**** **** **** " + cardEntity.getLastFourDigits();
    }

    @Override
    @Transactional
    public void rejectBlockRequest(long id) {
        logger.info("Rejecting block request with id {}", id);
        CardBlockRequestEntity entity = blockRequestRepo.findById(id)
                .orElseThrow(() -> new BlockRequestNotFoundException("No block request with id " + id));
        entity.setStatus(BlockRequestStatus.REJECTED);
        logger.info("Block request with id {} was successfully rejected.", id);
    }



    @Override
    @Transactional
    public List<CardEntity> getUserCards(int page, int size) {
        logger.info("Getting all user cards.");
        AccountCredentialsEntity credentials =
                (AccountCredentialsEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AccountEntity entity = accountRepo.findByCredentialsIdWithCards(credentials.getId());
        List<CardEntity> cards = entity.getCards().stream().skip((long) page * size).limit(size).toList();
        logger.info("Cards were got successfully.");
        return cards;
    }

    @Override
    @Transactional
    public CardEntity getCardByNumber(String number) {
        logger.info("Getting card with number {}.", number);
        AccountCredentialsEntity credentials =
                (AccountCredentialsEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AccountEntity entity = accountRepo.findByCredentialsIdWithCards(credentials.getId());
        CardEntity cardEntity = entity.getCards().stream()
                .filter(c -> c.getCardNumberEncrypted().equals(number)).findFirst()
                .orElseThrow(() -> new CardNotFoundException("No card with number " + number));
        logger.info("Card with number {} was successfully got.", number);
        return cardEntity;
    }

    @Override
    @Transactional
    public void createBlockRequest(String number) {
        logger.info("Creating block request to card with number {}.", number);
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
        logger.info("Block request to card with number {} was successfully created.", number);
    }

    @Override
    @Transactional
    public void transferMoney(String from, String to, double sum) {
        logger.info("Transfer money from card with number {} to card with number {}.", from, to);
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
        logger.info("Money from card with number {} to card with number {} were successfully transferred.", from, to);
    }
}