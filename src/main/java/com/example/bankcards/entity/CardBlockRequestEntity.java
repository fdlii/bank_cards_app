package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "card_block_request")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CardBlockRequestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    private CardEntity card;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private AccountEntity account;

    @Enumerated(value = EnumType.STRING)
    private BlockRequestStatus status;

    @Column(name = "created_at")
    private Instant createdAt;
}
