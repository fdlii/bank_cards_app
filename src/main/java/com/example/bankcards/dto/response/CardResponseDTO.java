package com.example.bankcards.dto.response;

import com.example.bankcards.entity.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CardResponseDTO {
    private long id;
    private String cardNumber;
    private double balance;
    private CardStatus status;
    private String validityPeriod;
}
