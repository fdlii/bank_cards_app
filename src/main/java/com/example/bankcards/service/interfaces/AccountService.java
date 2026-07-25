package com.example.bankcards.service.interfaces;

import com.example.bankcards.entity.AccountCredentialsEntity;

import java.nio.file.AccessDeniedException;

public interface AccountService {
    void registerAccount(AccountCredentialsEntity accountCredentialsEntity, String adminKey) throws AccessDeniedException;
    String loginAccount(String phoneNumber, String password);
}
