package com.example.bankcards.service.interfaces;

import com.example.bankcards.entity.AccountCredentialsEntity;
import com.example.bankcards.entity.AccountEntity;

import java.util.List;

public interface AccountService {
    long registerAccount(AccountCredentialsEntity accountCredentialsEntity, String adminKey) throws IllegalAccessException;
    String loginAccount(String phoneNumber, String password);
    void createUser(AccountEntity entity, long credentialsId);
    void deleteUser(long id);
    List<AccountEntity> getAllUsers(String firstName, String lastName, int page, int size);
}
