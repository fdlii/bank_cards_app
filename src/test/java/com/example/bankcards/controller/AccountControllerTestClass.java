package com.example.bankcards.controller;

import com.example.bankcards.config.CustomAccessDeniedHandler;
import com.example.bankcards.config.JwtAuthenticationEntryPoint;
import com.example.bankcards.dto.response.AccountResponseDTO;
import com.example.bankcards.entity.AccountCredentialsEntity;
import com.example.bankcards.entity.AccountEntity;
import com.example.bankcards.mapper.AccountCredentialsMapper;
import com.example.bankcards.mapper.AccountMapper;
import com.example.bankcards.security.BankUserDetailsService;
import com.example.bankcards.security.JwtFilter;
import com.example.bankcards.security.JwtHandler;
import com.example.bankcards.service.interfaces.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
public class AccountControllerTestClass {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private AccountService accountService;
    @MockitoBean private AccountMapper accountMapper;
    @MockitoBean private AccountCredentialsMapper accountCredentialsMapper;
    @MockitoBean private JwtFilter jwtFilter;
    @MockitoBean private JwtHandler jwtHandler;
    @MockitoBean private BankUserDetailsService bankUserDetailsService;
    @MockitoBean private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    @MockitoBean private CustomAccessDeniedHandler customAccessDeniedHandler;

    // ───── POST /account/register ─────

    @Test
    void register_success_returns200() throws Exception {
        when(accountCredentialsMapper.toEntity(any())).thenReturn(new AccountCredentialsEntity());
        when(accountService.registerAccount(any(), anyString())).thenReturn(1L);

        mockMvc.perform(post("/account/register")
                        .header("X-Admin-Key", "secret")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "phoneNumber": "+79991234567",
                                  "password": "password123",
                                  "role": "ROLE_USER"
                                }
                                """)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Account with id 1 was successfully created!"));
    }

    @Test
    void register_invalidPhonePattern_returns400() throws Exception {
        mockMvc.perform(post("/account/register")
                        .header("X-Admin-Key", "secret")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "phoneNumber": "not-a-phone",
                                  "password": "password123",
                                  "role": "ROLE_USER"
                                }
                                """)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_passwordTooShort_returns400() throws Exception {
        mockMvc.perform(post("/account/register")
                        .header("X-Admin-Key", "secret")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "phoneNumber": "+79991234567",
                                  "password": "short",
                                  "role": "ROLE_USER"
                                }
                                """)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // ───── POST /account/login ─────

    @Test
    void login_success_returns200() throws Exception {
        when(accountService.loginAccount(anyString(), anyString())).thenReturn("jwt.token.here");

        mockMvc.perform(post("/account/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "phoneNumber": "+79991234567",
                                  "password": "password123"
                                }
                                """)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("jwt.token.here"));
    }

    @Test
    void login_invalidPhonePattern_returns400() throws Exception {
        mockMvc.perform(post("/account/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "phoneNumber": "abc",
                                  "password": "password123"
                                }
                                """)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_blankPassword_returns400() throws Exception {
        mockMvc.perform(post("/account/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "phoneNumber": "+79991234567",
                                  "password": ""
                                }
                                """)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // ───── POST /account/create ─────

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_success_returns200() throws Exception {
        when(accountMapper.toEntity(any())).thenReturn(new AccountEntity());
        doNothing().when(accountService).createUser(any(), anyLong());

        mockMvc.perform(post("/account/create")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Иван",
                                  "lastName": "Иванов",
                                  "credentialsId": 1
                                }
                                """)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("User was successfully created."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_blankFirstName_returns400() throws Exception {
        mockMvc.perform(post("/account/create")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "",
                                  "lastName": "Иванов",
                                  "credentialsId": 1
                                }
                                """)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_negativeCredentialsId_returns400() throws Exception {
        mockMvc.perform(post("/account/create")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Иван",
                                  "lastName": "Иванов",
                                  "credentialsId": -1
                                }
                                """)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // ───── DELETE /account/{id} ─────

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_success_returns200() throws Exception {
        doNothing().when(accountService).deleteUser(anyLong());

        mockMvc.perform(delete("/account/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("User and his credentials were successfully deleted."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_negativeId_returns400() throws Exception {
        mockMvc.perform(delete("/account/-1")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteUser_unauthorized_returns401() throws Exception {
        mockMvc.perform(delete("/account/1")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    // ───── GET /account ─────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_success_returns200() throws Exception {
        AccountResponseDTO dto = new AccountResponseDTO();
        when(accountService.getAllUsers(any(), any(), any(Integer.class), any(Integer.class)))
                .thenReturn(List.of(new AccountEntity()));
        when(accountMapper.toDTOList(any())).thenReturn(List.of(dto));

        mockMvc.perform(get("/account")
                        .param("page", "0")
                        .param("size", "10")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_negativePage_returns400() throws Exception {
        mockMvc.perform(get("/account")
                        .param("page", "-1")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_zeroSize_returns400() throws Exception {
        mockMvc.perform(get("/account")
                        .param("size", "0")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}
