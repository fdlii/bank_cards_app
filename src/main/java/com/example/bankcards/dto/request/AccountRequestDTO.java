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
public class AccountRequestDTO {
    @NotBlank(message = "First name can't be empty.")
    private String firstName;

    @NotBlank(message = "Last name can't be empty.")
    private String lastName;

    @Min(value = 0, message = "Credentials id can't be less then 0.")
    private long credentialsId;
}
