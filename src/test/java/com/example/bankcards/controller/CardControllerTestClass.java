package com.example.bankcards.controller;

import com.example.bankcards.config.CustomAccessDeniedHandler;
import com.example.bankcards.config.JwtAuthenticationEntryPoint;
import com.example.bankcards.dto.response.CardBlockReqDTO;
import com.example.bankcards.dto.response.CardResponseDTO;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.mapper.CardBlockRequestMapper;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.security.BankUserDetailsService;
import com.example.bankcards.security.JwtFilter;
import com.example.bankcards.security.JwtHandler;
import com.example.bankcards.service.interfaces.CardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
public class CardControllerTestClass {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private CardService cardService;
    @MockitoBean private CardMapper cardMapper;
    @MockitoBean private CardBlockRequestMapper cardBlockRequestMapper;
    @MockitoBean private JwtFilter jwtFilter;
    @MockitoBean private JwtHandler jwtHandler;
    @MockitoBean private BankUserDetailsService bankUserDetailsService;
    @MockitoBean private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    @MockitoBean private CustomAccessDeniedHandler customAccessDeniedHandler;

    // ───── GET /card/admin ─────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCards_success_returns200() throws Exception {
        CardResponseDTO dto = new CardResponseDTO(1L, "**** **** **** 0001", 1000.0, CardStatus.ACTIVE, "12/2027");
        when(cardService.getAllCards(any(), any(), anyInt(), anyInt())).thenReturn(List.of(new CardEntity()));
        when(cardMapper.toDTOList(any())).thenReturn(List.of(dto));

        mockMvc.perform(get("/card/admin")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCards_negativePage_returns400() throws Exception {
        mockMvc.perform(get("/card/admin")
                        .param("page", "-1")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCards_zeroSize_returns400() throws Exception {
        mockMvc.perform(get("/card/admin")
                        .param("size", "0")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // ───── POST /card/admin ─────

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_success_returns200() throws Exception {
        when(cardMapper.toEntity(any())).thenReturn(new CardEntity());
        doNothing().when(cardService).createCard(any(), anyLong());

        mockMvc.perform(post("/card/admin")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": 1,
                                  "balance": 0.0,
                                  "validityPeriod": "12/2027"
                                }
                                """)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Card was successfully created."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_invalidValidityPeriodPattern_returns400() throws Exception {
        mockMvc.perform(post("/card/admin")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": 1,
                                  "balance": 0.0,
                                  "validityPeriod": "2027/12"
                                }
                                """)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_negativeBalance_returns400() throws Exception {
        mockMvc.perform(post("/card/admin")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": 1,
                                  "balance": -100.0,
                                  "validityPeriod": "12/2027"
                                }
                                """)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // ───── PATCH /card/admin/{id} ─────

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateCard_success_returns200() throws Exception {
        doNothing().when(cardService).activateCard(anyLong());

        mockMvc.perform(patch("/card/admin/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Card with id 1 was successfully activated."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateCard_negativeId_returns400() throws Exception {
        mockMvc.perform(patch("/card/admin/-1")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // ───── DELETE /card/admin/{id} ─────

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCard_success_returns200() throws Exception {
        doNothing().when(cardService).deleteCard(anyLong());

        mockMvc.perform(delete("/card/admin/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Card with id 1 was successfully deleted."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCard_negativeId_returns400() throws Exception {
        mockMvc.perform(delete("/card/admin/-1")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // ───── GET /card/admin/request ─────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getActiveBlockRequests_success_returns200() throws Exception {
        when(cardService.getActiveBlockRequests()).thenReturn(List.of());
        when(cardBlockRequestMapper.toDTOList(any())).thenReturn(List.of(new CardBlockReqDTO()));

        mockMvc.perform(get("/card/admin/request")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    // ───── PATCH /card/admin/request/approve/{id} ─────

    @Test
    @WithMockUser(roles = "ADMIN")
    void approveRequest_success_returns200() throws Exception {
        when(cardService.approveBlockRequest(anyLong())).thenReturn("**** **** **** 0001");

        mockMvc.perform(patch("/card/admin/request/approve/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Card with number **** **** **** 0001 was successfully blocked."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void approveRequest_negativeId_returns400() throws Exception {
        mockMvc.perform(patch("/card/admin/request/approve/-1")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // ───── PATCH /card/admin/request/reject/{id} ─────

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectRequest_success_returns200() throws Exception {
        doNothing().when(cardService).rejectBlockRequest(anyLong());

        mockMvc.perform(patch("/card/admin/request/reject/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Request with id 1 was rejected."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectRequest_negativeId_returns400() throws Exception {
        mockMvc.perform(patch("/card/admin/request/reject/-1")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // ───── GET /card/user ─────

    @Test
    @WithMockUser(roles = "USER")
    void getUserCards_success_returns200() throws Exception {
        when(cardService.getUserCards(anyInt(), anyInt())).thenReturn(List.of(new CardEntity()));
        when(cardMapper.toDTOList(any())).thenReturn(List.of(new CardResponseDTO()));

        mockMvc.perform(get("/card/user")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getUserCards_negativePage_returns400() throws Exception {
        mockMvc.perform(get("/card/user")
                        .param("page", "-1")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getUserCards_zeroSize_returns400() throws Exception {
        mockMvc.perform(get("/card/user")
                        .param("size", "0")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // ───── GET /card/user/{number} ─────

    @Test
    @WithMockUser(roles = "USER")
    void getCardByNumber_success_returns200() throws Exception {
        when(cardService.getCardByNumber(anyString())).thenReturn(new CardEntity());
        when(cardMapper.toDTO(any())).thenReturn(new CardResponseDTO());

        mockMvc.perform(get("/card/user/5412340000000001")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    // ───── POST /card/user/{number} ─────

    @Test
    @WithMockUser(roles = "USER")
    void createBlockRequest_success_returns200() throws Exception {
        doNothing().when(cardService).createBlockRequest(anyString());

        mockMvc.perform(post("/card/user/5412340000000001")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Request was successfully created."));
    }

    // ───── PATCH /card/user/transfer ─────

    @Test
    @WithMockUser(roles = "USER")
    void transferMoney_success_returns200() throws Exception {
        doNothing().when(cardService).transferMoney(anyString(), anyString(), anyDouble());

        mockMvc.perform(patch("/card/user/transfer")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "from": "5412340000000001",
                                  "to": "5412340000000002",
                                  "sum": 100.0
                                }
                                """)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Funds have been successfully delivered."));
    }

    @Test
    @WithMockUser(roles = "USER")
    void transferMoney_blankFromCard_returns400() throws Exception {
        mockMvc.perform(patch("/card/user/transfer")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "from": "",
                                  "to": "5412340000000002",
                                  "sum": 100.0
                                }
                                """)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void transferMoney_sumBelowMinimum_returns400() throws Exception {
        mockMvc.perform(patch("/card/user/transfer")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "from": "5412340000000001",
                                  "to": "5412340000000002",
                                  "sum": 5.0
                                }
                                """)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}
