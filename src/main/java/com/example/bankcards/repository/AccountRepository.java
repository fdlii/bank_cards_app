package com.example.bankcards.repository;

import com.example.bankcards.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {
    @Query("SELECT a FROM AccountEntity a LEFT JOIN FETCH a.cards WHERE a.credentials.id = :credentialsId")
    AccountEntity findByCredentialsIdWithCards(@Param("credentialsId") long credentialsId);
}
