package com.example.bankcards.service;

import com.example.bankcards.entity.AccountCredentialsEntity;
import com.example.bankcards.entity.AccountEntity;
import com.example.bankcards.exception.account.AccountNotFoundException;
import com.example.bankcards.exception.account.AuthException;
import com.example.bankcards.exception.account.CredentialsNotFoundException;
import com.example.bankcards.exception.account.InvalidRoleException;
import com.example.bankcards.repository.AccountCredentialsRepository;
import com.example.bankcards.repository.AccountRepository;
import com.example.bankcards.security.JwtHandler;
import com.example.bankcards.service.implementations.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTestClass {

    @Mock
    private AccountCredentialsRepository accountCredsRepo;

    @Mock
    private AccountRepository accountRepo;

    @Mock
    private JwtHandler jwtHandler;

    @Mock
    private AuthenticationManager authManager;

    @InjectMocks
    private AccountServiceImpl accountService;

    private AccountCredentialsEntity userCredentials;
    private AccountCredentialsEntity adminCredentials;
    private AccountCredentialsEntity savedCredentials;
    private AccountEntity account;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(accountService, "adminKey", "secret-admin-key");

        userCredentials = new AccountCredentialsEntity();
        userCredentials.setPhoneNumber("+79991234567");
        userCredentials.setPasswordHashed("password123");
        userCredentials.setRole("ROLE_USER");

        adminCredentials = new AccountCredentialsEntity();
        adminCredentials.setPhoneNumber("+79997654321");
        adminCredentials.setPasswordHashed("password123");
        adminCredentials.setRole("ROLE_ADMIN");

        savedCredentials = new AccountCredentialsEntity();
        savedCredentials.setId(1L);
        savedCredentials.setRole("ROLE_USER");

        account = new AccountEntity();
        account.setFirstName("Иван");
        account.setLastName("Иванов");
        account.setCredentials(savedCredentials);
    }

    // ───── registerAccount ─────

    @Test
    void registerAccount_success() throws IllegalAccessException {
        when(accountCredsRepo.save(any())).thenReturn(savedCredentials);

        long id = accountService.registerAccount(userCredentials, null);

        assertEquals(1L, id);
        verify(accountCredsRepo).save(any());
    }

    @Test
    void registerAccount_invalidRole_throwsInvalidRoleException() {
        userCredentials.setRole("ROLE_UNKNOWN");

        assertThrows(InvalidRoleException.class,
                () -> accountService.registerAccount(userCredentials, null));
    }

    @Test
    void registerAccount_adminWithInvalidKey_throwsIllegalAccessException() {
        assertThrows(IllegalAccessException.class,
                () -> accountService.registerAccount(adminCredentials, "wrong-key"));
    }

    // ───── loginAccount ─────

    @Test
    void loginAccount_success() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(accountCredsRepo.findByPhoneNumber(any())).thenReturn(Optional.of(userCredentials));
        when(jwtHandler.generateToken(any(), any())).thenReturn("jwt-token");

        String token = accountService.loginAccount(userCredentials.getPhoneNumber(), userCredentials.getPasswordHashed());

        assertEquals("jwt-token", token);
    }

    @Test
    void loginAccount_badCredentials_throwsAuthException() {
        when(authManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(AuthException.class,
                () -> accountService.loginAccount(userCredentials.getPhoneNumber(), "wrong-password"));
    }

    // ───── createUser ─────

    @Test
    void createUser_success() {
        when(accountCredsRepo.findById(1L)).thenReturn(Optional.of(savedCredentials));

        accountService.createUser(account, 1L);

        verify(accountRepo).save(account);
        assertEquals(savedCredentials, account.getCredentials());
    }

    @Test
    void createUser_credentialsNotFound_throwsCredentialsNotFoundException() {
        when(accountCredsRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CredentialsNotFoundException.class,
                () -> accountService.createUser(account, 99L));
    }

    // ───── deleteUser ─────

    @Test
    void deleteUser_success() {
        when(accountRepo.findById(1L)).thenReturn(Optional.of(account));

        accountService.deleteUser(1L);

        verify(accountCredsRepo).deleteById(savedCredentials.getId());
    }

    @Test
    void deleteUser_userNotFound_throwsAccountNotFoundException() {
        when(accountRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> accountService.deleteUser(99L));
    }

    // ───── getAllUsers ─────

    @Test
    void getAllUsers_success() {
        List<AccountEntity> users = List.of(account, new AccountEntity());
        when(accountRepo.findAllUsersWithFilters(any(), any(), any(PageRequest.class))).thenReturn(users);

        List<AccountEntity> result = accountService.getAllUsers(null, null, 0, 10);

        assertEquals(2, result.size());
    }
}
