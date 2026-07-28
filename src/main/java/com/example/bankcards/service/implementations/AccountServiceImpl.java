package com.example.bankcards.service.implementations;

import com.example.bankcards.entity.AccountCredentialsEntity;
import com.example.bankcards.entity.AccountEntity;
import com.example.bankcards.exception.account.AccountNotFoundException;
import com.example.bankcards.exception.account.AuthException;
import com.example.bankcards.exception.account.CredentialsNotFoundException;
import com.example.bankcards.exception.account.InvalidRoleException;
import com.example.bankcards.repository.AccountCredentialsRepository;
import com.example.bankcards.repository.AccountRepository;
import com.example.bankcards.service.interfaces.AccountService;
import com.example.bankcards.security.JwtHandler;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService {
    @Value("${auth.admin-key}")
    private String adminKey;

    private final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(8);
    private final AccountCredentialsRepository accountCredsRepo;
    private final AccountRepository accountRepo;
    private final JwtHandler jwtHandler;
    private final AuthenticationManager authManager;

    public AccountServiceImpl(AccountCredentialsRepository accountCredsRepo, JwtHandler jwtHandler, AuthenticationManager authManager, AccountRepository accountRepo) {
        this.accountCredsRepo = accountCredsRepo;
        this.jwtHandler = jwtHandler;
        this.authManager = authManager;
        this.accountRepo = accountRepo;
    }

    @Override
    @Transactional
    public long registerAccount(AccountCredentialsEntity accountCredentialsEntity, String adminKey) throws  IllegalAccessException {
        logger.info("Registering account with phone {}", accountCredentialsEntity.getPhoneNumber());
        if (!accountCredentialsEntity.getRole().equals("ROLE_USER") && !accountCredentialsEntity.getRole().equals("ROLE_ADMIN")) {
            throw new InvalidRoleException("Role " + accountCredentialsEntity.getRole() + " doesn't exist.");
        }
        if (accountCredentialsEntity.getRole().equals("ROLE_ADMIN")) {
            if (adminKey == null || !adminKey.equals(this.adminKey)) {
                throw new IllegalAccessException("Given admin key is invalid.");
            }
        }
        accountCredentialsEntity.setPasswordHashed(passwordEncoder.encode(accountCredentialsEntity.getPassword()));
        AccountCredentialsEntity result = accountCredsRepo.save(accountCredentialsEntity);
        logger.info("Account with phone {} was registered successfully.", accountCredentialsEntity.getPhoneNumber());
        return result.getId();
    }

    @Override
    @Transactional
    public String loginAccount(String phoneNumber, String password) {
        logger.info("Logining account with phone {}", phoneNumber);
        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(phoneNumber, password)
            );
            if (authentication.isAuthenticated()) {
                Optional<AccountCredentialsEntity> credentials = accountCredsRepo.findByPhoneNumber(phoneNumber);
                logger.info("Account with phone {} was logon successfully.", phoneNumber);
                return jwtHandler.generateToken(phoneNumber, credentials.get().getAuthorities());
            }
        }
        catch (BadCredentialsException e) {
            throw new AuthException("Incorrect password.");
        }
        catch (AuthenticationException e) {
            throw new AuthException(e.getMessage());
        }
        return "";
    }

    @Override
    @Transactional
    public void createUser(AccountEntity entity, long credentialsId) {
        logger.info("Creating user with name {}", entity.getFirstName());
        AccountCredentialsEntity credentials = accountCredsRepo.findById(credentialsId)
                .orElseThrow(() -> new CredentialsNotFoundException("No credentials with id " + credentialsId));
        entity.setCredentials(credentials);
        accountRepo.save(entity);
        logger.info("User with name {} was successfully created.", entity.getFirstName());
    }

    @Override
    @Transactional
    public void deleteUser(long id) {
        logger.info("Deleting user with id {}", id);
        AccountEntity user = accountRepo.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("No user with id " + id));
        accountCredsRepo.deleteById(user.getCredentials().getId());
        logger.info("User with id {} was successfully deleted.", id);
    }

    @Override
    public List<AccountEntity> getAllUsers(String firstName, String lastName, int page, int size) {
        logger.info("Getting all users.");
        Pageable pageable = PageRequest.of(page, size);
        List<AccountEntity> users = accountRepo.findAllUsersWithFilters(firstName, lastName, pageable);
        logger.info("Users was got successfully.");
        return users;
    }
}