package com.example.bankcards.entity;

import com.example.bankcards.util.CardNumberConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "card")
@NoArgsConstructor
@Getter
@Setter
public class CardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private AccountEntity account;

    @Column(name = "card_number_encrypted")
    @Convert(converter = CardNumberConverter.class)
    private String cardNumberEncrypted;

    private double balance;

    @Enumerated(value = EnumType.STRING)
    private CardStatus status;

    @Column(name = "validity_period")
    private String validityPeriod;

    @Column(name = "last_four_digits")
    private String lastFourDigits;
}
