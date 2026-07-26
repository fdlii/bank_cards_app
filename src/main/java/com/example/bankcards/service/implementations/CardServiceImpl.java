package com.example.bankcards.service.implementations;

import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.interfaces.CardService;
import org.springframework.stereotype.Service;

@Service
public class CardServiceImpl implements CardService {
    private final String BIN = "541234";
    private int counter = 0;

    private final CardRepository cardRepo;

    public CardServiceImpl(CardRepository cardRepo) {
        this.cardRepo = cardRepo;
    }
}
