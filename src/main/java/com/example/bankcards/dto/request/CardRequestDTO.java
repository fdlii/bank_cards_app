package com.example.bankcards.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CardRequestDTO {
    private long accountId;
    private double balance;
    private String validityPeriod;
}
