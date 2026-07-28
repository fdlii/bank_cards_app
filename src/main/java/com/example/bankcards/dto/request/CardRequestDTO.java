package com.example.bankcards.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CardRequestDTO {
    @Min(value = 0, message = "Account id can't be less then 0.")
    private long accountId;

    @Min(value = 0, message = "Balance can't be less then 0.")
    private double balance;

    @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{4}$", message = "Invalid validity period format.")
    private String validityPeriod;
}
