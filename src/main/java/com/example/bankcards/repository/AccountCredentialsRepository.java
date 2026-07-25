package com.example.bankcards.repository;

import com.example.bankcards.entity.AccountCredentialsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountCredentialsRepository extends JpaRepository<AccountCredentialsEntity, Long> {
    Optional<AccountCredentialsEntity> findByPhoneNumber(String phoneNumber);
}
