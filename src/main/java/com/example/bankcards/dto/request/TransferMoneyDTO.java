package com.example.bankcards.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TransferMoneyDTO {
    @NotBlank(message = "Specify the source card.")
    private String from;

    @NotBlank(message = "Specify the target card.")
    private String to;

    @Min(value = 10, message = "Transfer sum can't be less then 10.")
    private double sum;
}