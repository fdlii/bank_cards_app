package com.example.bankcards.controller;

import com.example.bankcards.dto.request.CardRequestDTO;
import com.example.bankcards.dto.request.TransferMoneyDTO;
import com.example.bankcards.dto.response.CardBlockReqDTO;
import com.example.bankcards.dto.response.CardResponseDTO;
import com.example.bankcards.mapper.CardBlockRequestMapper;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.service.interfaces.CardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/card")
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
    public ResponseEntity<List<CardResponseDTO>> getAllCards() {
        return ResponseEntity.ok().body(cardMapper.toDTOList(cardService.getAllCards()));
    }

    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> createCard(
            @RequestBody CardRequestDTO cardRequestDTO
    ) {
        cardService.createCard(cardMapper.toEntity(cardRequestDTO), cardRequestDTO.getAccountId());
        return ResponseEntity.ok("Card was successfully created.");
    }

    @PatchMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> activateCard(@PathVariable("id") long id) {
        cardService.activateCard(id);
        return ResponseEntity.ok("Card with id " + id + " was successfully activated.");
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteCard(@PathVariable("id") long id) {
        cardService.deleteCard(id);
        return ResponseEntity.ok("Card with id " + id + " was successfully deleted.");
    }

    @GetMapping("/admin/request")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CardBlockReqDTO>> getActiveBlockRequests() {
        return ResponseEntity.ok().body(cardBlockRequestMapper.toDTOList(cardService.getActiveBlockRequests()));
    }

    @PatchMapping("/admin/request/approve/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> approveRequest(@PathVariable("id") long id) {
        String cardNumber = cardService.approveBlockRequest(id);
        return ResponseEntity.ok("Card with number " + cardNumber + " was successfully blocked.");
    }

    @PatchMapping("/admin/request/reject/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> rejectRequest(@PathVariable("id") long id) {
        cardService.rejectBlockRequest(id);
        return ResponseEntity.ok("Request with id " + id + " was rejected.");
    }

    //User

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<CardResponseDTO>> getUserCards() {
        return ResponseEntity.ok(cardMapper.toDTOList(cardService.getUserCards()));
    }

    @GetMapping("/user/{number}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CardResponseDTO> getCardByNumber(@PathVariable("number") String number) {
        return ResponseEntity.ok(cardMapper.toDTO(cardService.getCardByNumber(number)));
    }

    @PostMapping("/user/{number}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> createBlockRequest(@PathVariable("number") String number) {
        cardService.createBlockRequest(number);
        return ResponseEntity.ok("Request was successfully created.");
    }

    @PatchMapping("/user/transfer")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> transferMoney(@RequestBody TransferMoneyDTO transferMoneyDTO) {
        cardService.transferMoney(transferMoneyDTO.getFrom(), transferMoneyDTO.getTo(), transferMoneyDTO.getSum());
        return ResponseEntity.ok("Funds have been successfully delivered.");
    }
}
