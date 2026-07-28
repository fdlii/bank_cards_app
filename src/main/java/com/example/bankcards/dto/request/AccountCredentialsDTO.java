package com.example.bankcards.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AccountCredentialsDTO {
    @NotBlank(message = "Phone number can't be empty.")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format.")
    private String phoneNumber;

    @NotBlank(message = "Password can't be empty.")
    @Size(min = 8, message = "Password can't be less than 8 symbols.")
    private String password;

    @NotNull(message = "Any user must have a role.")
    private String role;
}
