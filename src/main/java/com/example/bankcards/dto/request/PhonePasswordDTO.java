package com.example.bankcards.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PhonePasswordDTO {
    @NotBlank(message = "Phone number can't be empty.")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format.")
    private String phoneNumber;

    @NotBlank(message = "Password can't be empty.")
    @Size(min = 8, message = "Password can't be less than 8 symbols.")
    private String password;
}
