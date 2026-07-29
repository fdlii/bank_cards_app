package com.example.bankcards.repository;

import com.example.bankcards.entity.CardBlockRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardBlockRequestRepository extends JpaRepository<CardBlockRequestEntity, Long> {
    @Query("SELECT r FROM CardBlockRequestEntity r JOIN FETCH r.card JOIN FETCH r.account WHERE r.status = 'PENDING'")
    List<CardBlockRequestEntity> getActiveBlockRequests();
}
