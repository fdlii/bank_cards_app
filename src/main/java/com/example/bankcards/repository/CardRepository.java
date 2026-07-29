package com.example.bankcards.repository;

import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.CardStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<CardEntity, Long> {
    @Query("SELECT c FROM CardEntity c WHERE " +
            "(LOWER(c.account.firstName) = LOWER(:firstName) OR :firstName IS NULL) AND " +
            "(LOWER(c.account.lastName) = LOWER(:lastName) OR :lastName IS NULL)")
    List<CardEntity> findAllCardsWithFilters(@Param("firstName") String firstName,
                                             @Param("lastName") String lastName,
                                             Pageable pageable);

    List<CardEntity> findAllByStatusNot(CardStatus status);
}
