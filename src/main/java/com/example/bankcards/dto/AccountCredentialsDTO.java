package com.example.bankcards.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AccountCredentialsDTO {
    private String phoneNumber;
    private String password;
    private String role;
}
