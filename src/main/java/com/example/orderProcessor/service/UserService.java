package com.example.orderProcessor.service;

import com.example.orderProcessor.entity.JwtAuthenticationEntity;
import com.example.orderProcessor.entity.JwtResponseEntity;
import com.example.orderProcessor.entity.UserEntity;
import com.example.orderProcessor.exception.*;
import com.example.orderProcessor.repository.UserRepo;
import jakarta.security.auth.message.AuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import io.jsonwebtoken.Claims;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private JwtProvider jwtProvider;
    private final Map<String, String> refreshStorage = new HashMap<>();

    private BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();

    public UserEntity registration(UserEntity user) throws UserAlreadyExistException, UserIncorrectUsernameException, UserIncorrectEmailException, UserIncorrectPasswordException, UserIncorrectRoleException {
        if (user.getUsername() == null) {
            throw new UserIncorrectUsernameException("Укажите логин");
        }
        if (user.getUsername().length() > 50 || user.getUsername().isEmpty()) {
            throw new UserIncorrectUsernameException("Длина логина должна быть не менее 1 символа и не более 50.");
        }
        if (user.getEmail() == null) {
            throw new UserIncorrectEmailException("Укажите почту");
        }
        if (user.getEmail().length() > 100 || user.getEmail().isEmpty()) {
            throw new UserIncorrectEmailException("Длина почты должна быть не менее 1 символа и не более 100.");
        }
        if (!user.getEmail().contains("@")) {
            throw new UserIncorrectEmailException("Почта не содержит символа @.");
        }
        if (user.getPassword_hash() == null) {
            throw new UserIncorrectPasswordException("Укажите пароль");
        }
        if (user.getPassword_hash().length() > 255 || user.getPassword_hash().isEmpty()) {
            throw new UserIncorrectPasswordException("Длина пароля должна быть не менее 1 символа и не более 255.");
        }
        if (user.getRole() == null) {
            throw new UserIncorrectRoleException("Укажите роль пользователя");
        }
        if (!List.of("customer", "chef", "manager").contains(user.getRole())) {
            throw new UserIncorrectRoleException("Данной роли пользователя не существует.");
        }

        if (userRepo.findByUsername(user.getUsername()) != null) {
            throw new UserAlreadyExistException("Пользователь c таким логином уже существует");
        }
        if (userRepo.findByEmail(user.getEmail()) != null) {
            throw new UserAlreadyExistException("Пользователь с такой почтой уже существует");
        }

        user.setPassword_hash(bcryptEncoder.encode(user.getPassword_hash()));
        user.setCreate_at(new Timestamp(System.currentTimeMillis()));
        user.setUpdated_at(new Timestamp(System.currentTimeMillis()));

        return userRepo.save(user);
    }

    public JwtResponseEntity login(UserEntity user) throws UserIncorrectEmailException, UserIncorrectPasswordException {
        if (user.getEmail() == null) {
            throw new UserIncorrectEmailException("Укажите почту");
        }
        if (user.getPassword_hash() == null) {
            throw new UserIncorrectPasswordException("Укажите пароль");
        }
        UserEntity userData = userRepo.findByEmail(user.getEmail());

        if (userData == null) {
            throw new UserIncorrectEmailException("Пользователя с такой почтой не существует");
        }
        if (!bcryptEncoder.matches(user.getPassword_hash(), userData.getPassword_hash())) {
            throw new UserIncorrectPasswordException("Пароль введён некорректно.");
        }

        final String accessToken = jwtProvider.generateAccessToken(userData);
        final String refreshToken = jwtProvider.generateRefreshToken(userData);
        refreshStorage.put(userData.getUsername(), refreshToken);
        userData.setUpdated_at(new Timestamp(System.currentTimeMillis()));
        return new JwtResponseEntity(accessToken, refreshToken);
    }

    public JwtResponseEntity getAccessToken(@NonNull String refreshToken) throws AuthException {
        if (jwtProvider.validateRefreshToken(refreshToken)) {
            Claims claims = jwtProvider.getRefreshClaims(refreshToken);
            String username = claims.getSubject();
            String saveRefreshToken = refreshStorage.get(username);
            if (saveRefreshToken != null && saveRefreshToken.equals(refreshToken)) {
                UserEntity userData = userRepo.findByUsername(username);
                if (userData == null) {
                    throw new AuthException("Пользователь не найден");
                }
                final String accessToken = jwtProvider.generateAccessToken(userData);
                return new JwtResponseEntity(accessToken, null);
            }
        }
        return new JwtResponseEntity(null, null);
    }

    public JwtResponseEntity refresh(@NonNull String refreshToken) throws AuthException {
        if (jwtProvider.validateRefreshToken(refreshToken)) {
            final Claims claims = jwtProvider.getRefreshClaims(refreshToken);
            final String username = claims.getSubject();
            final String saveRefreshToken = refreshStorage.get(username);
            if (saveRefreshToken != null && saveRefreshToken.equals(refreshToken)) {
                UserEntity userData = userRepo.findByUsername(username);
                if (userData == null) {
                    throw new AuthException("Пользователь не найден");
                }
                final String accessToken = jwtProvider.generateAccessToken(userData);
                final String newRefreshToken = jwtProvider.generateRefreshToken(userData);
                refreshStorage.put(userData.getUsername(), newRefreshToken);
                return new JwtResponseEntity(accessToken, newRefreshToken);
            }
        }
        throw new AuthException("Невалидный JWT токен");
    }

    public JwtAuthenticationEntity getAuthInfo() {
        return (JwtAuthenticationEntity)SecurityContextHolder.getContext().getAuthentication();
    }
}
