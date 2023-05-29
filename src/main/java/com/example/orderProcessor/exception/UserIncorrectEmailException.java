package com.example.orderProcessor.exception;

public class UserIncorrectEmailException extends Throwable {
    public UserIncorrectEmailException(String message) {
        super(message);
    }
}
