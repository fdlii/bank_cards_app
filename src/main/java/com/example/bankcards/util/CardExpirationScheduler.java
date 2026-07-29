package com.example.bankcards.util;

import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.repository.CardRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 Раз в сутки проверяет срок действия всех карт. Если validityPeriod (формат MM/yyyy)
 уже прошёл относительно текущего месяца, карте выставляется статус EXPIRED.
 **/
@Component
public class CardExpirationScheduler {

    private static final DateTimeFormatter VALIDITY_FORMAT = DateTimeFormatter.ofPattern("MM/yyyy");

    private final Logger logger = LoggerFactory.getLogger(CardExpirationScheduler.class);
    private final CardRepository cardRepo;

    public CardExpirationScheduler(CardRepository cardRepo) {
        this.cardRepo = cardRepo;
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Europe/Moscow")
    @Transactional
    public void expireOutdatedCards() {
        logger.info("Starting scheduled check of card validity periods.");
        YearMonth currentMonth = YearMonth.now();

        List<CardEntity> cards = cardRepo.findAllByStatusNot(CardStatus.EXPIRED);
        int expiredCount = 0;

        for (CardEntity card : cards) {
            if (isExpired(card, currentMonth)) {
                card.setStatus(CardStatus.EXPIRED);
                expiredCount++;
                logger.info("Card with id {} was marked as EXPIRED (validity period: {}).",
                        card.getId(), card.getValidityPeriod());
            }
        }

        logger.info("Scheduled check finished. {} card(s) were marked as EXPIRED.", expiredCount);
    }

    private boolean isExpired(CardEntity card, YearMonth currentMonth) {
        String validityPeriod = card.getValidityPeriod();
        if (validityPeriod == null || validityPeriod.isBlank()) {
            return false;
        }
        try {
            YearMonth cardMonth = YearMonth.parse(validityPeriod, VALIDITY_FORMAT);
            return currentMonth.isAfter(cardMonth);
        } catch (Exception e) {
            logger.warn("Card with id {} has invalid validity period '{}', skipping.",
                    card.getId(), validityPeriod);
            return false;
        }
    }
}
