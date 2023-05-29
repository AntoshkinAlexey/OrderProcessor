package com.example.orderProcessor.exception;

public class UserIncorrectPasswordException extends Throwable {
    public UserIncorrectPasswordException(String message) {
        super(message);
    }
}
