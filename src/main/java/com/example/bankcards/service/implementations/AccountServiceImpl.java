package com.example.bankcards.service.implementations;

import com.example.bankcards.entity.AccountCredentialsEntity;
import com.example.bankcards.exception.AuthException;
import com.example.bankcards.exception.InvalidRoleException;
import com.example.bankcards.repository.AccountCredentialsRepository;
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

import java.nio.file.AccessDeniedException;
import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService {
    @Value("${auth.admin-key}")
    private String adminKey;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(8);
    private final AccountCredentialsRepository accountCredsRepo;
    private final JwtHandler jwtHandler;
    private final AuthenticationManager authManager;

    public AccountServiceImpl(AccountCredentialsRepository accountCredsRepo, JwtHandler jwtHandler, AuthenticationManager authManager) {
        this.accountCredsRepo = accountCredsRepo;
        this.jwtHandler = jwtHandler;
        this.authManager = authManager;
    }

    @Override
    @Transactional
    public void registerAccount(AccountCredentialsEntity accountCredentialsEntity, String adminKey) throws AccessDeniedException {
        if (!accountCredentialsEntity.getRole().equals("ROLE_USER") && !accountCredentialsEntity.getRole().equals("ROLE_ADMIN")) {
            throw new InvalidRoleException("Given role doesn't exist.");
        }
        if (accountCredentialsEntity.getRole().equals("ROLE_ADMIN")) {
            if (adminKey == null || !adminKey.equals(this.adminKey)) {
                throw new AccessDeniedException("Given admin key is invalid.");
            }
        }
        accountCredentialsEntity.setPasswordHashed(passwordEncoder.encode(accountCredentialsEntity.getPassword()));
        accountCredsRepo.save(accountCredentialsEntity);
    }

    @Override
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
}
