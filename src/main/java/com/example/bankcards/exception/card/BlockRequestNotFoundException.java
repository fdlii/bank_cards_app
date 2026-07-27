package com.example.bankcards.exception.card;

public class BlockRequestNotFoundException extends RuntimeException {
    public BlockRequestNotFoundException(String message) {
        super(message);
    }
}
