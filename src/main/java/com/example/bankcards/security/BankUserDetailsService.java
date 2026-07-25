package com.example.bankcards.security;

import com.example.bankcards.entity.AccountCredentialsEntity;
import com.example.bankcards.repository.AccountCredentialsRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class BankUserDetailsService implements UserDetailsService {
    private final AccountCredentialsRepository accountCredentialsRepository;

    public BankUserDetailsService(AccountCredentialsRepository accountCredentialsRepository) {
        this.accountCredentialsRepository = accountCredentialsRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String phoneNumber) throws UsernameNotFoundException {
        AccountCredentialsEntity accountCredentialsEntity = accountCredentialsRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UsernameNotFoundException("Account with such phone number doesn't exist."));
        return accountCredentialsEntity;
    }
}
