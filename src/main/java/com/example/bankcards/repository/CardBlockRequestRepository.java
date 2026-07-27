package com.example.bankcards.repository;

import com.example.bankcards.entity.CardBlockRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardBlockRequestRepository extends JpaRepository<CardBlockRequestEntity, Long> {
    @Query(value = "SELECT c FROM CardBlockRequestEntity c WHERE c.status == BlockRequestStatus.PENDING")
    List<CardBlockRequestEntity> getActiveBlockRequests();
}
