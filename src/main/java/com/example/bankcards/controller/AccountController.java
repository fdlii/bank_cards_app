package com.example.bankcards.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/account")
public class AccountController {

    public AccountController() {

    }

    @GetMapping("/register")
    public ResponseEntity<String> register() {
        return ResponseEntity.status(200).body("First allowed method.");
    }
}
