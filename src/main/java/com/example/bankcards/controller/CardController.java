package com.example.bankcards.controller;

import com.example.bankcards.dto.FIO;
import com.example.bankcards.dto.request.CardRequestDTO;
import com.example.bankcards.dto.request.TransferMoneyDTO;
import com.example.bankcards.dto.response.CardBlockReqDTO;
import com.example.bankcards.dto.response.CardResponseDTO;
import com.example.bankcards.dto.response.MessageResponse;
import com.example.bankcards.mapper.CardBlockRequestMapper;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.service.interfaces.CardService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/card")
@Validated
public class CardController {
    private final CardService cardService;
    private final CardMapper cardMapper;
    private final CardBlockRequestMapper cardBlockRequestMapper;

    public CardController(CardService cardService, CardMapper cardMapper, CardBlockRequestMapper cardBlockRequestMapper) {
        this.cardService = cardService;
        this.cardMapper = cardMapper;
        this.cardBlockRequestMapper = cardBlockRequestMapper;
    }

    //Admin

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CardResponseDTO>> getAllCards(
            @ModelAttribute FIO fio,
            @Min(value = 0, message = "Page can't be less then 0.")
            @RequestParam(defaultValue = "0") int page,
            @Min(value = 1, message = "Size can't be less then 1.")
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok().body(cardMapper.toDTOList(cardService.getAllCards(fio.getFirstName(), fio.getLastName(), page, size)));
    }

    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> createCard(
            @Valid @RequestBody CardRequestDTO cardRequestDTO
    ) {
        cardService.createCard(cardMapper.toEntity(cardRequestDTO), cardRequestDTO.getAccountId());
        return ResponseEntity.ok(new MessageResponse("Card was successfully created."));
    }

    @PatchMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> activateCard(
            @Min(value = 0, message = "Card id can't be less then 0.")
            @PathVariable("id") long id) {
        cardService.activateCard(id);
        return ResponseEntity.ok(new MessageResponse("Card with id " + id + " was successfully activated."));
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteCard(
            @Min(value = 0, message = "Card id can't be less then 0.")
            @PathVariable("id") long id) {
        cardService.deleteCard(id);
        return ResponseEntity.ok(new MessageResponse("Card with id " + id + " was successfully deleted."));
    }

    @GetMapping("/admin/request")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CardBlockReqDTO>> getActiveBlockRequests() {
        return ResponseEntity.ok().body(cardBlockRequestMapper.toDTOList(cardService.getActiveBlockRequests()));
    }

    @PatchMapping("/admin/request/approve/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> approveRequest(
            @Min(value = 0, message = "Request id can't be less then 0.")
            @PathVariable("id") long id) {
        String cardNumber = cardService.approveBlockRequest(id);
        return ResponseEntity.ok(new MessageResponse("Card with number " + cardNumber + " was successfully blocked."));
    }

    @PatchMapping("/admin/request/reject/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> rejectRequest(
            @Min(value = 0, message = "Request id can't be less then 0.")
            @PathVariable("id") long id) {
        cardService.rejectBlockRequest(id);
        return ResponseEntity.ok(new MessageResponse("Request with id " + id + " was rejected."));
    }

    //User

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<CardResponseDTO>> getUserCards(
            @Min(value = 0, message = "Page can't be less then 0.")
            @RequestParam(defaultValue = "0") int page,
            @Min(value = 1, message = "Size can't be less then 1.")
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(cardMapper.toDTOList(cardService.getUserCards(page, size)));
    }

    @GetMapping("/user/{number}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CardResponseDTO> getCardByNumber(
            @PathVariable("number") String number) {
        return ResponseEntity.ok(cardMapper.toDTO(cardService.getCardByNumber(number)));
    }

    @PostMapping("/user/{number}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MessageResponse> createBlockRequest(
            @PathVariable("number") String number) {
        cardService.createBlockRequest(number);
        return ResponseEntity.ok(new MessageResponse("Request was successfully created."));
    }

    @PatchMapping("/user/transfer")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MessageResponse> transferMoney(
            @Valid @RequestBody TransferMoneyDTO transferMoneyDTO) {
        cardService.transferMoney(transferMoneyDTO.getFrom(), transferMoneyDTO.getTo(), transferMoneyDTO.getSum());
        return ResponseEntity.ok(new MessageResponse("Funds have been successfully delivered."));
    }
}