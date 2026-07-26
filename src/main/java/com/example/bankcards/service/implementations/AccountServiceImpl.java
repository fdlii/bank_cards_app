package com.example.bankcards.service.implementations;

import com.example.bankcards.entity.AccountCredentialsEntity;
import com.example.bankcards.entity.AccountEntity;
import com.example.bankcards.exception.AccountNotFoundException;
import com.example.bankcards.exception.AuthException;
import com.example.bankcards.exception.CredentialsNotFoundException;
import com.example.bankcards.exception.InvalidRoleException;
import com.example.bankcards.repository.AccountCredentialsRepository;
import com.example.bankcards.repository.AccountRepository;
import com.example.bankcards.service.interfaces.AccountService;
import com.example.bankcards.security.JwtHandler;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService {
    @Value("${auth.admin-key}")
    private String adminKey;

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
    public long registerAccount(AccountCredentialsEntity accountCredentialsEntity, String adminKey) throws AccessDeniedException {
        if (!accountCredentialsEntity.getRole().equals("ROLE_USER") && !accountCredentialsEntity.getRole().equals("ROLE_ADMIN")) {
            throw new InvalidRoleException("Given role doesn't exist.");
        }
        if (accountCredentialsEntity.getRole().equals("ROLE_ADMIN")) {
            if (adminKey == null || !adminKey.equals(this.adminKey)) {
                throw new AccessDeniedException("Given admin key is invalid.");
            }
        }
        accountCredentialsEntity.setPasswordHashed(passwordEncoder.encode(accountCredentialsEntity.getPassword()));
        return accountCredsRepo.save(accountCredentialsEntity).getId();
    }

    @Override
    @Transactional
    public String loginAccount(String phoneNumber, String password) {
        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(phoneNumber, password)
            );
            if (authentication.isAuthenticated()) {
                Optional<AccountCredentialsEntity> credentials = accountCredsRepo.findByPhoneNumber(phoneNumber);
                return jwtHandler.generateToken(phoneNumber, credentials.get().getAuthorities());
            }
        }
        catch (InternalAuthenticationServiceException e) {
            throw new AuthException(e.getMessage());
        }
        catch (BadCredentialsException e) {
            throw new AuthException("Incorrect password.");
        }
        return "";
    }

    @Override
    @Transactional
    public void createUser(AccountEntity entity, long credentialsId) {
        AccountCredentialsEntity credentials = accountCredsRepo.findById(credentialsId)
                .orElseThrow(() -> new CredentialsNotFoundException("No credentials with such id."));
        entity.setCredentials(credentials);
        accountRepo.save(entity);
    }

    @Override
    @Transactional
    public void deleteUser(long id) {
        AccountEntity user = accountRepo.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("No user with such id"));
        accountCredsRepo.deleteById(user.getCredentials().getId());
    }

    @Override
    @Transactional
    public List<AccountEntity> getAllUsers() {
        return accountRepo.findAll();
    }
}
