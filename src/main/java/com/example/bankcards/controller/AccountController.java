package com.example.bankcards.controller;

import com.example.bankcards.dto.request.AccountCredentialsDTO;
import com.example.bankcards.dto.request.AccountRequestDTO;
import com.example.bankcards.dto.response.AccountResponseDTO;
import com.example.bankcards.dto.request.PhonePasswordDTO;
import com.example.bankcards.mapper.AccountCredentialsMapper;
import com.example.bankcards.mapper.AccountMapper;
import com.example.bankcards.service.interfaces.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/account")
public class AccountController {
    private final AccountService accountService;
    private final AccountCredentialsMapper accountCredentialsMapper;
    private final AccountMapper accountMapper;

    public AccountController(AccountService accountService, AccountCredentialsMapper accountCredentialsMapper, AccountMapper accountMapper) {
        this.accountService = accountService;
        this.accountCredentialsMapper = accountCredentialsMapper;
        this.accountMapper = accountMapper;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestHeader("X-Admin-Key") String adminKey,
            @RequestBody AccountCredentialsDTO accountCredentialsDTO
    ) throws AccessDeniedException {
        long id = accountService.registerAccount(accountCredentialsMapper.toEntity(accountCredentialsDTO), adminKey);
        return ResponseEntity.ok("Account with id " + id + " was successfully created!");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(
            @RequestBody PhonePasswordDTO phonePasswordDTO
    )
    {
        String token = accountService.loginAccount(phonePasswordDTO.getPhoneNumber(), phonePasswordDTO.getPassword());
        return ResponseEntity.ok(token);
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> createUser(
            @RequestBody AccountRequestDTO accountDTO
    ) {
        accountService.createUser(accountMapper.toEntity(accountDTO), accountDTO.getCredentialsId());
        return ResponseEntity.ok("User was successfully created.");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable("id") long id) {
        accountService.deleteUser(id);
        return ResponseEntity.ok("User and his credentials were successfully deleted.");
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AccountResponseDTO>> getAllUsers() {
        return ResponseEntity.status(200).body(accountMapper.toDTOList(accountService.getAllUsers()));
    }

}
