package com.example.bankcards.controller;

import com.example.bankcards.dto.AccountCredentialsDTO;
import com.example.bankcards.dto.PhonePasswordDTO;
import com.example.bankcards.mapper.AccountCredentialsMapper;
import com.example.bankcards.service.interfaces.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/account")
public class AccountController {
    private final AccountService accountService;
    private final AccountCredentialsMapper accountCredentialsMapper;

    public AccountController(AccountService accountService, AccountCredentialsMapper accountCredentialsMapper) {
        this.accountService = accountService;
        this.accountCredentialsMapper = accountCredentialsMapper;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestHeader("X-Admin-Key") String adminKey,
            @RequestBody AccountCredentialsDTO accountCredentialsDTO
    ) throws AccessDeniedException {
        accountService.registerAccount(accountCredentialsMapper.toEntity(accountCredentialsDTO), adminKey);
        return ResponseEntity.ok("Account was successfully created!");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(
            @RequestBody PhonePasswordDTO phonePasswordDTO
    )
    {
        String token = accountService.loginAccount(phonePasswordDTO.getPhoneNumber(), phonePasswordDTO.getPassword());
        return ResponseEntity.ok(token);
    }

    @GetMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> testSec() {
        return ResponseEntity.ok("Security works!");
    }
}
