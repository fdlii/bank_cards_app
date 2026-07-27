package com.example.bankcards.repository;

import com.example.bankcards.entity.CardEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<CardEntity, Long> {
    @Query("SELECT c FROM CardEntity c WHERE " +
            "(:firstName IS NULL OR LOWER(c.account.firstName) = LOWER(:firstName)) AND " +
            "(:lastName IS NULL OR LOWER(c.account.lastName) = LOWER(:lastName))")
    List<CardEntity> findAllCardsWithFilters(@Param("firstName") String firstName,
                                             @Param("lastName") String lastName,
                                             Pageable pageable);
}
