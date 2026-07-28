package com.example.bankcards.service;

import com.example.bankcards.entity.*;
import com.example.bankcards.exception.account.AccountNotFoundException;
import com.example.bankcards.exception.card.BlockRequestNotFoundException;
import com.example.bankcards.exception.card.CardNotFoundException;
import com.example.bankcards.exception.card.InsufficientFundsException;
import com.example.bankcards.exception.card.InvalidCardStatusException;
import com.example.bankcards.repository.AccountRepository;
import com.example.bankcards.repository.CardBlockRequestRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.implementations.CardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardServiceTestClass {

    @Mock
    private AccountRepository accountRepo;

    @Mock
    private CardRepository cardRepo;

    @Mock
    private CardBlockRequestRepository blockRequestRepo;

    @InjectMocks
    private CardServiceImpl cardService;

    private AccountCredentialsEntity credentials;
    private AccountEntity account;
    private CardEntity activeCard;
    private CardEntity secondActiveCard;
    private CardEntity blockedCard;
    private CardBlockRequestEntity blockRequest;

    @BeforeEach
    void setUp() {
        credentials = new AccountCredentialsEntity();
        credentials.setId(1L);
        credentials.setRole("ROLE_USER");

        activeCard = new CardEntity();
        activeCard.setId(1L);
        activeCard.setCardNumberEncrypted("5412340000000001");
        activeCard.setLastFourDigits("0001");
        activeCard.setStatus(CardStatus.ACTIVE);
        activeCard.setBalance(1000.0);

        secondActiveCard = new CardEntity();
        secondActiveCard.setId(2L);
        secondActiveCard.setCardNumberEncrypted("5412340000000002");
        secondActiveCard.setLastFourDigits("0002");
        secondActiveCard.setStatus(CardStatus.ACTIVE);
        secondActiveCard.setBalance(500.0);

        blockedCard = new CardEntity();
        blockedCard.setId(3L);
        blockedCard.setCardNumberEncrypted("5412340000000003");
        blockedCard.setLastFourDigits("0003");
        blockedCard.setStatus(CardStatus.BLOCKED);
        blockedCard.setBalance(0.0);

        account = new AccountEntity();
        account.setId(1L);
        account.setFirstName("Иван");
        account.setLastName("Иванов");
        account.setCredentials(credentials);
        account.setCards(new ArrayList<>(List.of(activeCard, secondActiveCard, blockedCard)));

        blockRequest = CardBlockRequestEntity.builder()
                .id(1L)
                .card(activeCard)
                .account(account)
                .status(BlockRequestStatus.PENDING)
                .build();

        // Устанавливаем SecurityContext для методов, читающих текущего пользователя
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(credentials, null, credentials.getAuthorities())
        );
    }

    // ───── getAllCards ─────

    @Test
    void getAllCards_success() {
        when(cardRepo.findAllCardsWithFilters(any(), any(), any())).thenReturn(List.of(activeCard, secondActiveCard));

        List<CardEntity> result = cardService.getAllCards(null, null, 0, 10);

        assertEquals(2, result.size());
        assertTrue(result.get(0).getCardNumberEncrypted().startsWith("**** **** ****"));
    }

    // ───── createCard ─────

    @Test
    void createCard_success() {
        CardEntity newCard = new CardEntity();
        newCard.setId(10L);

        when(accountRepo.findById(1L)).thenReturn(Optional.of(account));
        when(cardRepo.save(any())).thenReturn(newCard);

        cardService.createCard(new CardEntity(), 1L);

        verify(cardRepo).save(any());
    }

    @Test
    void createCard_accountNotFound_throwsAccountNotFoundException() {
        when(accountRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> cardService.createCard(new CardEntity(), 99L));
    }

    // ───── activateCard ─────

    @Test
    void activateCard_success() {
        when(cardRepo.findById(1L)).thenReturn(Optional.of(activeCard));

        cardService.activateCard(1L);

        assertEquals(CardStatus.ACTIVE, activeCard.getStatus());
    }

    @Test
    void activateCard_cardNotFound_throwsCardNotFoundException() {
        when(cardRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class,
                () -> cardService.activateCard(99L));
    }

    // ───── deleteCard ─────

    @Test
    void deleteCard_success() {
        when(cardRepo.findById(1L)).thenReturn(Optional.of(activeCard));

        cardService.deleteCard(1L);

        verify(cardRepo).delete(activeCard);
    }

    @Test
    void deleteCard_cardNotFound_throwsCardNotFoundException() {
        when(cardRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class,
                () -> cardService.deleteCard(99L));
    }

    // ───── getActiveBlockRequests ─────

    @Test
    void getActiveBlockRequests_success() {
        when(blockRequestRepo.getActiveBlockRequests()).thenReturn(List.of(blockRequest));

        List<CardBlockRequestEntity> result = cardService.getActiveBlockRequests();

        assertEquals(1, result.size());
    }

    // ───── approveBlockRequest ─────

    @Test
    void approveBlockRequest_success() {
        when(blockRequestRepo.findById(1L)).thenReturn(Optional.of(blockRequest));

        String result = cardService.approveBlockRequest(1L);

        assertEquals(BlockRequestStatus.APPROVED, blockRequest.getStatus());
        assertEquals(CardStatus.BLOCKED, activeCard.getStatus());
        assertTrue(result.contains("0001"));
    }

    @Test
    void approveBlockRequest_notFound_throwsBlockRequestNotFoundException() {
        when(blockRequestRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BlockRequestNotFoundException.class,
                () -> cardService.approveBlockRequest(99L));
    }

    // ───── rejectBlockRequest ─────

    @Test
    void rejectBlockRequest_success() {
        when(blockRequestRepo.findById(1L)).thenReturn(Optional.of(blockRequest));

        cardService.rejectBlockRequest(1L);

        assertEquals(BlockRequestStatus.REJECTED, blockRequest.getStatus());
    }

    @Test
    void rejectBlockRequest_notFound_throwsBlockRequestNotFoundException() {
        when(blockRequestRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BlockRequestNotFoundException.class,
                () -> cardService.rejectBlockRequest(99L));
    }

    // ───── getUserCards ─────

    @Test
    void getUserCards_success() {
        when(accountRepo.findByCredentialsIdWithCards(1L)).thenReturn(account);

        List<CardEntity> result = cardService.getUserCards(0, 10);

        assertEquals(3, result.size());
    }

    // ───── getCardByNumber ─────

    @Test
    void getCardByNumber_success() {
        when(accountRepo.findByCredentialsIdWithCards(1L)).thenReturn(account);

        CardEntity result = cardService.getCardByNumber("5412340000000001");

        assertEquals(activeCard, result);
    }

    @Test
    void getCardByNumber_notFound_throwsCardNotFoundException() {
        when(accountRepo.findByCredentialsIdWithCards(1L)).thenReturn(account);

        assertThrows(CardNotFoundException.class,
                () -> cardService.getCardByNumber("0000000000000000"));
    }

    // ───── createBlockRequest ─────

    @Test
    void createBlockRequest_success() {
        when(accountRepo.findByCredentialsIdWithCards(1L)).thenReturn(account);

        cardService.createBlockRequest("5412340000000001");

        verify(blockRequestRepo).save(any());
    }

    @Test
    void createBlockRequest_cardNotFound_throwsCardNotFoundException() {
        when(accountRepo.findByCredentialsIdWithCards(1L)).thenReturn(account);

        assertThrows(CardNotFoundException.class,
                () -> cardService.createBlockRequest("0000000000000000"));
    }

    // ───── transferMoney ─────

    @Test
    void transferMoney_success() {
        when(accountRepo.findByCredentialsIdWithCards(1L)).thenReturn(account);

        cardService.transferMoney("5412340000000001", "5412340000000002", 200.0);

        assertEquals(800.0, activeCard.getBalance());
        assertEquals(700.0, secondActiveCard.getBalance());
    }

    @Test
    void transferMoney_cardNotFound_throwsCardNotFoundException() {
        when(accountRepo.findByCredentialsIdWithCards(1L)).thenReturn(account);

        assertThrows(CardNotFoundException.class,
                () -> cardService.transferMoney("0000000000000000", "5412340000000002", 100.0));
    }

    @Test
    void transferMoney_cardNotActive_throwsInvalidCardStatusException() {
        when(accountRepo.findByCredentialsIdWithCards(1L)).thenReturn(account);

        assertThrows(InvalidCardStatusException.class,
                () -> cardService.transferMoney("5412340000000001", "5412340000000003", 100.0));
    }

    @Test
    void transferMoney_insufficientFunds_throwsInsufficientFundsException() {
        when(accountRepo.findByCredentialsIdWithCards(1L)).thenReturn(account);

        assertThrows(InsufficientFundsException.class,
                () -> cardService.transferMoney("5412340000000001", "5412340000000002", 9999.0));
    }
}
