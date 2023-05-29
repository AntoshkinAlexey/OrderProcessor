package com.example.orderProcessor.service;

import com.example.orderProcessor.entity.JwtAuthenticationEntity;
import io.jsonwebtoken.Claims;

public final class JwtUtils {
    public static JwtAuthenticationEntity generate(Claims claims) {
        final JwtAuthenticationEntity jwtInfoToken = new JwtAuthenticationEntity();
        jwtInfoToken.setRole(claims.get("role", String.class));
        jwtInfoToken.setUsername(claims.getSubject());
        return jwtInfoToken;
    }
}
