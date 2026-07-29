package com.example.bankcards.repository;

import com.example.bankcards.entity.AccountEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {
    @Query("SELECT a FROM AccountEntity a LEFT JOIN FETCH a.cards WHERE a.credentials.id = :credentialsId")
    AccountEntity findByCredentialsIdWithCards(@Param("credentialsId") long credentialsId);

    @Query("SELECT a FROM AccountEntity a WHERE " +
            "(LOWER(a.firstName) = LOWER(:firstName) OR :firstName IS NULL) AND " +
            "(LOWER(a.lastName) = LOWER(:lastName) OR :lastName IS NULL)")
    List<AccountEntity> findAllUsersWithFilters(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            Pageable pageable);
}