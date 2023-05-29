package com.example.orderProcessor.entity;

public class JwtResponseEntity {

    private final String type = "Bearer";
    private String accessToken;
    private String refreshToken;

    public JwtResponseEntity(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}